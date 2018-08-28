package Expenses.TestUtils

import Expenses.Model.{Claim, Employee, ExpenseSheet}
import cats.data.StateT

object AcceptanceTestUtils {
  case class TestState(employees: List[Employee],
                       expenseSheets: List[ExpenseSheet],
                       claims: List[Claim])

  type OrError[A] = Either[Throwable, A]

  type Test[A] = StateT[OrError, TestState, A]
}
