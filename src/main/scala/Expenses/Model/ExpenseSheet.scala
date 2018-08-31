package Expenses.Model

import java.util.UUID

import Expenses.Utils.ErrorManagement
import Expenses.Utils.ErrorManagement.Validated
import cats.implicits._
import ExpenseSheet.implicits._

sealed case class ExpenseSheetId(uuid: UUID)

sealed trait ExpenseSheet {
  def id: ExpenseSheetId
  def employee: Employee
  def expenses: List[Expense]
}

case class OpenExpenseSheet private (id: ExpenseSheetId,
                             employee: Employee,
                             expenses:List[Expense]) extends ExpenseSheet

case class ClaimedExpenseSheet private (id: ExpenseSheetId,
                                employee: Employee,
                                expenses:List[Expense]) extends ExpenseSheet

object ExpenseSheet {
  private val validateId = ErrorManagement.notNull[ExpenseSheetId]("id is null")(_)
  private val validateEmployee = ErrorManagement.notNull[Employee]("employee is null")(_)

  def createOpen(id: ExpenseSheetId, employee: Employee, expenses:List[Expense]) : Validated[OpenExpenseSheet] =
    (validateId(id), validateEmployee(employee))
      .mapN(OpenExpenseSheet(_, _, expenses))

  def createOpen(employee: Employee, expenses:List[Expense]) : Validated[OpenExpenseSheet] =
    createOpen(UUID.randomUUID(), employee, expenses)

  private val validateExpenses = ErrorManagement.nonEmptyList[Expense]("expenses is empty")(_)

  def createClaimed(id: ExpenseSheetId, employee: Employee, expenses:List[Expense]) : Validated[ClaimedExpenseSheet] =
    (validateId(id), validateEmployee(employee), validateExpenses(expenses))
      .mapN(ClaimedExpenseSheet)

  def createClaimed(employee: Employee, expenses:List[Expense]) : Validated[ClaimedExpenseSheet] =
    createClaimed(UUID.randomUUID(), employee, expenses)

  object implicits {
    import scala.language.implicitConversions

    implicit def uuidToExpenseSheetId(uuid: UUID) : ExpenseSheetId = ExpenseSheetId(uuid)
  }
}
