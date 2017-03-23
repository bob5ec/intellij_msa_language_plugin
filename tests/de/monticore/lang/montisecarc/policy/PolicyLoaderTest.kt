package de.monticore.lang.montisecarc.policy

import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import org.junit.Assert.*

/**
 * Copyright 2017 thomasbuning
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class PolicyLoaderTest: LightCodeInsightFixtureTestCase() {

    override fun getTestDataPath(): String {
        return "testData/policy"
    }

    fun testPolicyLoader() {

        //ToDo add XSD
        //ToDo add Library
        //myFixture.configureFromTempProjectFile()
    }
}