package de.monticore.lang.montisecarc.generator

import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import de.monticore.lang.montisecarc.psi.*

/**
 * Copyright 2016 Thomas Buning
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class GraphGenerator {

    private val files = mutableListOf<PsiFile>()
    private val registeredGenerators = mutableMapOf<IElementType, List<Pair<MSAGenerator, (Any) -> Unit>>>()
    private val trustLevels = mutableSetOf<Int>()
    private val nodes = mutableSetOf<String>()
    private val connectors = mutableSetOf<String>()
    private var psiRecursiveElementWalkingVisitor: MSAPsiRecursiveElementWalkingVisitor? = null
    private var referencedComponentInstances = mutableListOf<Pair<PsiFile, MSAComponentDeclaration>>()
    /**
     * Key is the super component that needs to be replaced by the value
     */
    private var componentExtendHierarchy = mutableMapOf<String, String>()

    fun createGraph(): String {

        return files.map { generate(it) }.filter { !it.isNullOrEmpty() }.joinToString("")
    }

    private fun isSubComponent(componentQualifiedNames: List<String>, qualifiedName: String): Boolean
            = componentQualifiedNames.filter { it != qualifiedName }.any { qualifiedName.contains(it) }

    private fun generate(parseFile: PsiFile): String? {

        if (psiRecursiveElementWalkingVisitor != null) {
            ApplicationManager.getApplication().runReadAction({

                parseFile.accept(psiRecursiveElementWalkingVisitor!!)

                val referencedComponentNames = referencedComponentInstances.map { it.second.qualifiedName }.union(superComponents.map { it.second.qualifiedName }).toList()
                referencedComponentInstances.union(superComponents).forEach {

                    if (it.first != parseFile && !isSubComponent(referencedComponentNames, it.second.qualifiedName)) {

                        it.second.accept(psiRecursiveElementWalkingVisitor!!)
                    }
                }

                /*
                * - We got the original file (parseFile) and all files referenced by Instances
                * - Now we need to collect all ports and components etc from superClasses and combine them in the extending class
                * - Ports could be already imported from references, or components so it is needed to check if these are already included
                * - SuperClasses don't need to be included because port etc. shall be combined in the extending class
                * */
                for ((superComponent, extendingComponent) in componentExtendHierarchy) {

                    // Remove Super Components
                    nodes.removeAll { it -> it.startsWith("($superComponent:Component") }

                    // Fix Links
                    connectors.mapInPlace { it.replace("($superComponent)", "($extendingComponent)") }
                }

            })

            trustLevels.forEach {
                nodes.add(TrustLevelGenerator.generateTrustLevelNode(it))
            }

            nodes.addAll(connectors)

            if (nodes.isEmpty()) {
                return null
            }

            //CREATE ${nodes};
            val model = mutableMapOf<String, String>()
            model.put("nodes", nodes.filter { !it.isNullOrEmpty() }.joinToString())
            val graph = FreeMarker.instance.generateModelOutput("ToGraph/Create.ftl", model)
            return graph
        }
        return null
    }

    inline fun <T> MutableSet<T>.mapInPlace(mutator: (T)->T) {
        val iterate = this.iterator()
        val nodesToUpdate = mutableListOf<Pair<T, T>>()
        while (iterate.hasNext()) {
            val oldValue = iterate.next()
            val newValue = mutator(oldValue)
            if (newValue !== oldValue) {

                nodesToUpdate.add(Pair(oldValue, newValue))
            }
        }

        for ((oldValue, newValue) in nodesToUpdate) {
            this.remove(oldValue)
            this.add(newValue)
        }
    }

    fun addFile(psiFile: PsiFile) = files.add(psiFile)

    fun registerGenerator(elementType: IElementType, generator: MSAGenerator, callback: (Any) -> Unit) {
        val pair = listOf(Pair(generator, callback))
        if (registeredGenerators.contains(elementType)) {

            registeredGenerators.put(elementType, registeredGenerators[elementType]!! + pair)

        } else {

            registeredGenerators.put(elementType, pair)
        }

        psiRecursiveElementWalkingVisitor = MSAPsiRecursiveElementWalkingVisitor(registeredGenerators)
    }

    private val superComponents = mutableListOf<Pair<PsiFile, MSAComponentDeclaration>>()

    fun registerDefaultGenerators() {

        fun extractStringsFromList(addTo: MutableSet<String>): (Any) -> Unit {
            return {
                connector ->
                if (connector is List<*>) {
                    connector.forEach {
                        if (it is String) {
                            addTo.add(it)
                        }
                    }
                }
            }
        }

        fun extractStringsFromListAndReferences(addTo: MutableSet<String>, references: MutableList<Pair<PsiFile, MSAComponentDeclaration>>): (Any) -> Unit {
            return {
                connector ->
                if (connector is Triple<*, *, *>) {

                    try {
                        val containingFile = connector.first as PsiFile
                        val referencedComponentInstance = connector.second as MSAComponentDeclaration
                        val nodes = connector.third as List<*>


                        nodes.forEach {
                            if (it is String) {
                                addTo.add(it)
                            }
                        }

                        references.add(Pair(containingFile, referencedComponentInstance))
                    } catch(e: Exception) {
                    }
                }
            }
        }

        fun extractString(addTo: MutableSet<String>): (Any) -> Unit {
            return {
                component ->
                if (component is String) {
                    addTo.add(component)
                }
            }
        }

        fun extractTrustLevel(): (Any) -> Unit {
            return {

                componentTrustLevel ->
                if (componentTrustLevel is Pair<*, *>) {

                    if (componentTrustLevel.first is Int && componentTrustLevel.second is List<*>) {

                        val componentIdentifier = componentTrustLevel.second as List<*>
                        componentIdentifier.forEach {
                            if (it is String) {
                                connectors.add(it)
                            }
                        }
                        val trustLevel = componentTrustLevel.first as Int
                        trustLevels.add(trustLevel)
                    }
                }
            }
        }

        registerGenerator(MSACompositeElementTypes.COMPONENT_DECLARATION, ComponentDeclarationGenerator(), extractString(nodes))

        registerGenerator(MSACompositeElementTypes.COMPONENT_DECLARATION, ComponentInstanceGenerator(), extractString(nodes))

        registerGenerator(MSACompositeElementTypes.COMPONENT_DECLARATION, ComponentDeclarationConnectorGenerator(), extractStringsFromList(connectors))

        registerGenerator(MSACompositeElementTypes.COMPONENT_DECLARATION, PortComponentInstanceDeclarationConnectorGenerator(), extractStringsFromList(connectors))

        registerGenerator(MSACompositeElementTypes.COMPONENT_DECLARATION, ComponentHierarchyConnectorGenerator(), extractStringsFromList(connectors))

        registerGenerator(MSACompositeElementTypes.COMPONENT_DECLARATION, SuperComponentGenerator(), {

            foundComponents ->
            if (foundComponents is List<*>) {


                foundComponents.forEach {

                    if (it is Triple<*, *, *>) {

                        if (it.first is PsiFile && it.second is MSAComponentDeclaration) {
                            superComponents.add(Pair(it.first as PsiFile, it.second as MSAComponentDeclaration))

                            if (it.third is MSAComponentDeclaration) {

                                componentExtendHierarchy.put((it.second as MSAComponentDeclaration).qualifiedName.replace(".", "_"), (it.third as MSAComponentDeclaration).qualifiedName.replace(".", "_"))
                            }
                        }
                    }
                }
            }
        })

        registerGenerator(MSACompositeElementTypes.PORT_ELEMENT, PortElementGenerator(), extractString(nodes))

        registerGenerator(MSACompositeElementTypes.PORT_ELEMENT, PortElementConnectorGenerator(), extractStringsFromList(connectors))

        registerGenerator(MSACompositeElementTypes.COMPONENT_INSTANCE_DECLARATION, ComponentInstanceInstanceGenerator(), extractStringsFromListAndReferences(nodes, referencedComponentInstances))

        registerGenerator(MSACompositeElementTypes.COMPONENT_INSTANCE_DECLARATION, PortComponentInstanceConnectorGenerator(), extractStringsFromList(connectors))

        registerGenerator(MSACompositeElementTypes.COMPONENT_INSTANCE_DECLARATION, ComponentInstanceDeclarationConnectorGenerator(), extractStringsFromList(connectors))

        registerGenerator(MSACompositeElementTypes.IDENTITY_STATEMENT, IdentityStatementGenerator(), extractStringsFromList(connectors))

        registerGenerator(MSACompositeElementTypes.CONNECTOR, ConnectorGenerator(), extractStringsFromList(connectors))

        registerGenerator(MSACompositeElementTypes.CONNECTOR, ConnectorIdentityGenerator(), extractStringsFromList(connectors))


        /**
         *
         * ToDo: Extends in Instance Declarations
         */



        registerGenerator(MSACompositeElementTypes.COMPONENT_DECLARATION, ComponentTrustLevelGenerator(), extractTrustLevel())
    }
}

class ConnectorIdentityGenerator : MSAGenerator() {

    fun getIdentifier(psiElement: PsiElement, componentInstanceNames: List<MSAComponentInstanceName>): String? {
        var identifier: String? = null
        if (componentInstanceNames.isEmpty()) {

            // Source is Enclosing Component
            val startComponent = PsiTreeUtil.getParentOfType(psiElement, MSAComponentDeclaration::class.java)

            if (startComponent != null) {
                identifier = ComponentDeclarationGenerator.createComponentIdentifier(startComponent)
            }
        } else {

            val startComponent = componentInstanceNames.last().references[0].resolve()

            if (startComponent != null) {

                val msaComponentDeclaration = PsiTreeUtil.getParentOfType(startComponent, MSAComponentDeclaration::class.java)
                val msaComponentInstanceDeclaration = PsiTreeUtil.getParentOfType(startComponent, MSAComponentInstanceDeclaration::class.java)

                if (msaComponentDeclaration != null) {

                    identifier = ComponentDeclarationGenerator.createComponentIdentifier(msaComponentDeclaration)
                } else if (msaComponentInstanceDeclaration != null) {

                    val msaComponentName = msaComponentInstanceDeclaration.componentNameWithTypeProjectionList.last().componentName.references[0].resolve()

                    val componentDeclaration = PsiTreeUtil.getParentOfType(msaComponentName, MSAComponentDeclaration::class.java)

                    if (componentDeclaration != null && componentDeclaration is MSAComponentDeclaration) {

                        identifier = ComponentDeclarationGenerator.createComponentIdentifier(componentDeclaration)
                    }
                }
            }
        }
        return identifier
    }

    override fun generate(psiElement: PsiElement): Any? {

        if(psiElement is MSAConnector) {

            val weak = psiElement.node.findChildByType(MSATokenElementTypes.WEAK)
            val strong = psiElement.node.findChildByType(MSATokenElementTypes.STRONG)
            
            if(weak != null || strong != null) {

                val startComponentInstanceNames = psiElement.connectSource.qualifiedIdentifier.componentInstanceNameList

                val startIdentifier = getIdentifier(psiElement, startComponentInstanceNames)
                if(!startIdentifier.isNullOrEmpty()) {


                    val connectors = mutableListOf<String>()

                    val link = if(weak != null) ":WEAK" else ":STRONG"
                    psiElement.connectTargetList.forEach {

                        val stopComponentInstanceNames = it.qualifiedIdentifier.componentInstanceNameList

                        val stopIdentifier = getIdentifier(psiElement, stopComponentInstanceNames)

                        if(!stopIdentifier.isNullOrEmpty()) {
                            val connector_model = mutableMapOf<String, Any>()
                            connector_model.put("relationship_type", link)
                            connector_model.put("start_port", startIdentifier!!)
                            connector_model.put("target_port", stopIdentifier!!)
                            connector_model.put("element_offset", psiElement.textOffset)
                            val connector = FreeMarker.instance.generateModelOutput("ToGraph/ConnectorMacro.ftl", connector_model)

                            connectors.add(connector)
                        }
                    }

                    return connectors
                }
            }
        }
        return null
    }

}

class SuperComponentGenerator : MSAGenerator() {

    override fun generate(psiElement: PsiElement): Any? {

        if (psiElement is MSAComponentDeclaration) {

            return psiElement.superComponents.map {
                Triple(it.containingFile, it, psiElement)
            }
        }

        return null
    }
}
