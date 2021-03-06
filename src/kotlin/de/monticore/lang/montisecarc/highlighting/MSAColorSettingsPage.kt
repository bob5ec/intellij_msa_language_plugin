package de.monticore.lang.montisecarc.highlighting

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import de.monticore.lang.montisecarc.MSAIcons
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import javax.swing.Icon

/**
 *  Copyright 2016 thomasbuning

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
class MSAColorSettingsPage : ColorSettingsPage {

    private val DESCRIPTORS = arrayOf(AttributesDescriptor("Key", MSASyntaxHighlighter.KEY),
            AttributesDescriptor("String", MSASyntaxHighlighter.STRING),
            AttributesDescriptor("Comment", MSASyntaxHighlighter.SINGLE_LINE_COMMENT),
            AttributesDescriptor("Level", MSASyntaxHighlighter.LEVEL),
            AttributesDescriptor("Component Name", MSASyntaxHighlighter.COMPONENT_NAME),
            AttributesDescriptor("Parameters", MSASyntaxHighlighter.TYPES),
            AttributesDescriptor("Componenent Instance Name", MSASyntaxHighlighter.COMPONENT_INSTANCE_NAME),
            AttributesDescriptor("Suppress Policy Annotation", MSASyntaxHighlighter.SUPPRESSION_ANNOTATION_KEYWORD)
    )

    @Nullable
    override fun getIcon(): Icon {
        return MSAIcons.FILE
    }

    @NotNull
    override fun getHighlighter(): SyntaxHighlighter {
        return MSASyntaxHighlighter()
    }

    @NotNull
    override fun getDemoText(): String {
        return "package secarc.supermarket;\n" +
                "\n" +
                "import secarc.supermarket.msg.*;\n" +
                "\n" +
                "import secarc.supermarket.imp.CashDesk;\n" +
                "import secarc.supermarket.imp.WebSite;\n" +
                "\n" +
                "@SupressPolicy(\"PolicyIdentifier\")\n" +
                "component Supermarket {\n" +
                "\n" +
                "	trustlevel +0;\n" +
                "	accesscontrol off;\n" +
                "	\n" +
                "	// cash desk ports	\n" +
                "   @SupressPolicy(\"PolicyIdentifier\")\n" +
                "	port\n" +
                "		in Event newSale,\n" +
                "		in Image barcode,\n" +
                "		out ProductData outPDataNS,\n" +
                "		in Event endSale;\n" +
                "		\n" +
                "	CashDesk cashDesk;\n" +
                "	\n" +
                "	connect newSale -> cashDesk.newSale;\n" +
                "	connect barcode -> cashDesk.barcode;\n" +
                "	connect cashDesk.outPDataNS -> outPDataNS;\n" +
                "	connect endSale -> cashDesk.endSale;\n" +
                "	\n" +
                "	port\n" +
                "		in Event newOrder,\n" +
                "		in Integer productId,\n" +
                "		out ProductData outPDataNO,\n" +
                "		in Event endOrder;\n" +
                "	port \n" +
                "		in Event payOnlineWeb,\n" +
                "		in AccountData accDataPOW,\n" +
                "		in Integer pinPOW,\n" +
                "		out Boolean outValidationPOW;\n" +
                "\n" +
                "}"
    }

    @Nullable
    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? {
        return null
    }

    @NotNull
    override fun getAttributeDescriptors(): Array<AttributesDescriptor> {
        return DESCRIPTORS
    }

    @NotNull
    override fun getColorDescriptors(): Array<ColorDescriptor> {
        return ColorDescriptor.EMPTY_ARRAY
    }

    @NotNull
    override fun getDisplayName(): String {
        return "MontiSecArc"
    }
}