package Infrastructure.InMemory

import Expenses.Model.ExpenseSheet.ExpenseSheetId
import Expenses.Model.{Employee, ExpenseSheet}
import Expenses.Repositories.{ExpenseSheetRepository, ExpenseSheetRepositoryME}
import Expenses.TestUtils.AcceptanceTestUtils.{Test, TestME, TestState}
import Expenses.TestUtils.{InMemoryExpenseSheetRepository, InMemoryExpenseSheetRepositoryME}
import Infrastructure.{ExpenseSheetRepositoryContractTest, ExpenseSheetRepositoryMEContractTest}
import cats.data.State
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

  override def run[A](toBeExecuted: Test[A]): A = toBeExecuted.runA(state).value

  override def cleanUp(expenseSheetIds: List[ExpenseSheetId]): Unit = ()

  override def existExpenseSheet(id: ExpenseSheetId): Test[Boolean] =
    State.get.map(_.expenseSheets.count(_.id == id) == 1)
}

class InMemoryExpenseSheetRepositoryMETest extends ExpenseSheetRepositoryMEContractTest[TestME] {
  implicit var state : TestState = _

  override def createRepositoryWith(expenseSheets: List[ExpenseSheet], employees: List[Employee]):
    ExpenseSheetRepositoryME[TestME] = {
      state = TestState(
        employees,
        expenseSheets,
        List())
      new InMemoryExpenseSheetRepositoryME
    }

  def run[A](toBeExecuted: TestME[A]) : Either[Throwable, A] = toBeExecuted.runA(state)

  override def cleanUp(expenseSheetIds: List[ExpenseSheetId]): Unit = ()
}
