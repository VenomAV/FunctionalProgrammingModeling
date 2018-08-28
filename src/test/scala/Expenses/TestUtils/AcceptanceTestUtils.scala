package Expenses.TestUtils

import Expenses.Model.{Claim, Employee, ExpenseSheet}
import cats.data.State

object AcceptanceTestUtils {
  case class TestState(employees: List[Employee],
                       expenseSheets: List[ExpenseSheet],
                       claims: List[Claim])

  type Test[A] = State[TestState, A]
}
