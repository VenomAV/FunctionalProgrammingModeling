package Infrastructure.InMemory

import Expenses.Model.ExpenseSheet.ExpenseSheetId
import Expenses.Model.{Employee, ExpenseSheet}
import Expenses.Repositories.ExpenseSheetRepository
import Expenses.TestUtils.AcceptanceTestUtils.{Test, TestState}
import Expenses.TestUtils.InMemoryExpenseSheetRepository
import Infrastructure.ExpenseSheetRepositoryContractTest
import cats.implicits._

class InMemoryExpenseSheetRepositoryTest extends ExpenseSheetRepositoryContractTest[Test] {
  implicit var state : TestState = _

  override def createRepositoryWith(expenseSheets: List[ExpenseSheet], employees: List[Employee]):
    ExpenseSheetRepository[Test] = {
      state = TestState(
        employees,
        expenseSheets,
        List())
      new InMemoryExpenseSheetRepository
    }

  def run[A](toBeExecuted: Test[A]) : Either[Throwable, A] = toBeExecuted.runA(state)

  override def cleanUp(expenseSheetIds: List[ExpenseSheetId]): Unit = ()
}
