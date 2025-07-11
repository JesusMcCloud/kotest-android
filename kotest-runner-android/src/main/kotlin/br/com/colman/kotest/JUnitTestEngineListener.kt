package br.com.colman.kotest

import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.engine.listener.AbstractTestEngineListener
import io.kotest.engine.test.names.DefaultDisplayNameFormatter
import org.junit.runner.Description
import org.junit.runner.Description.createTestDescription
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier

class JUnitTestEngineListener(
   private val notifier: RunNotifier,
) : AbstractTestEngineListener() {

   private val formatter = DefaultDisplayNameFormatter()

   override suspend fun testStarted(testCase: TestCase) {
      notifier.fireTestStarted(describeTestCase(testCase, formatter.format(testCase)))
   }

   override suspend fun testFinished(testCase: TestCase, result: TestResult) {
      val desc = describeTestCase(testCase, formatter.format(testCase))
      when (result) {
         is TestResult.Success -> notifier.fireTestFinished(desc)
         is TestResult.Error -> notifyFailure(desc, result)
         is TestResult.Ignored -> notifier.fireTestIgnored(desc)
         is TestResult.Failure -> notifyFailure(desc, result)
      }
   }

   private fun notifyFailure(desc: Description, result: TestResult) {
      notifier.fireTestFailure(Failure(desc, result.errorOrNull))
      notifier.fireTestFinished(desc)
   }
}

internal fun describeTestCase(testCase: TestCase, displayName: String) = createTestDescription(testCase.spec::class.java,displayName)
