package Expenses.Model

import java.util.UUID

import Expenses.Model.ExpenseSheet.ExpenseSheetId
import Expenses.Utils.ErrorManagement
import Expenses.Utils.ErrorManagement.ValidationResult
import cats.implicits._

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
  type ExpenseSheetId = UUID

  private val validateId = ErrorManagement.notNull[ExpenseSheetId]("id is null")(_)
  private val validateEmployee = ErrorManagement.notNull[Employee]("employee is null")(_)

  def createOpen(id: ExpenseSheetId, employee: Employee, expenses:List[Expense]) : ValidationResult[OpenExpenseSheet] =
    (validateId(id), validateEmployee(employee))
      .mapN(OpenExpenseSheet(_, _, expenses))

  def createOpen(employee: Employee, expenses:List[Expense]) : ValidationResult[OpenExpenseSheet] =
    createOpen(UUID.randomUUID(), employee, expenses)

  private val validateExpenses = ErrorManagement.nonEmptyList[Expense]("expenses is empty")(_)

  def createClaimed(id: ExpenseSheetId, employee: Employee, expenses:List[Expense]) : ValidationResult[ClaimedExpenseSheet] =
    (validateId(id), validateEmployee(employee), validateExpenses(expenses))
      .mapN(ClaimedExpenseSheet)

  def createClaimed(employee: Employee, expenses:List[Expense]) : ValidationResult[ClaimedExpenseSheet] =
    createClaimed(UUID.randomUUID(), employee, expenses)
}
