/*
 * Copyright 2020 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.zup.beagle.android.view.viewmodel

import android.view.View
import br.com.zup.beagle.android.action.AnalyticsAction
import br.com.zup.beagle.android.widget.RootView
import br.com.zup.beagle.newanalytics.AnalyticsService
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Given an Analytics View Model")
class AnalyticsViewModelTest {

    private val analyticsViewModel = AnalyticsViewModel()
    private val rootView: RootView = mockk()
    private val origin: View = mockk()
    private val action: AnalyticsAction = mockk()
    private val analyticsValue: String = "any"

    @BeforeEach
    fun setUp() {
        mockkObject(AnalyticsService)
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(AnalyticsService)
    }

    @DisplayName("When create action report")
    @Nested
    inner class CreateActionReport {

        @DisplayName("Should call Analytics Service with correct parameters")
        @Test
        fun testCreateActionReportShouldCallCorrectFun() {
            //given
            every {
                AnalyticsService.createActionRecord(
                    rootView,
                    origin,
                    action,
                    analyticsValue
                )
            } just Runs

            //when
            analyticsViewModel.createActionReport(rootView, origin, action, analyticsValue)

            //then
            verify(exactly = 1) {
                AnalyticsService.createActionRecord(rootView, origin, action, analyticsValue)
            }
        }
    }

    @DisplayName("When create screen report")
    @Nested
    inner class CreateScreenReport {

        @DisplayName("Should call Analytics Service with correct parameters")
        @Test
        fun testCreateScreenReportShouldCallCorrectFun() {
            //given
            every { AnalyticsService.createScreenRecord("screenId") } just Runs

            //when
            analyticsViewModel.createScreenReport("screenId")

            //then
            verify(exactly = 1) {
                AnalyticsService.createScreenRecord("screenId")
            }
        }
    }
}
