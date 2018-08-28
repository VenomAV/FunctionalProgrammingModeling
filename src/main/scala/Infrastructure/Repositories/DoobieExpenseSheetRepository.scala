package Infrastructure.Repositories

import Expenses.Model.ExpenseSheet.ExpenseSheetId
import Expenses.Model._
import Expenses.Repositories.ExpenseSheetRepository
import Infrastructure.Repositories.Doobie.implicits._
import cats.MonadError
import cats.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.invariant.UnexpectedEnd

class DoobieExpenseSheetRepository(implicit ME: MonadError[ConnectionIO, Throwable]) extends ExpenseSheetRepository[ConnectionIO] {

  type ExpenseSheetType = String
  type DBTuple = (ExpenseSheetId, ExpenseSheetType, List[Expense], Employee)

  override def get(id: ExpenseSheetId): ConnectionIO[ExpenseSheet] =
    sql"""select es.id, es.type, es.expenses, e.id, e.name, e.surname
          from expensesheets es
          join employees e on e.id = es.employeeid
          where es.id=$id"""
      .query[DBTuple]
      .unique
      .recoverWith({
        case UnexpectedEnd => ME.raiseError(new Error(s"Unable to find expense sheet $id"))
      }).flatMap(unsafeDBTupleToExpenseSheet)

  private def unsafeDBTupleToExpenseSheet(tuple: DBTuple) : ConnectionIO[ExpenseSheet] = {
    val (id, expenseSheetType, expenses, employee) = tuple

    expenseSheetType match {
      case "O" => ME.pure(OpenExpenseSheet(id, employee, expenses))
      case "C" => ME.pure(ClaimedExpenseSheet(id, employee, expenses))
      case _ => ME.raiseError(new Error("Expense sheet type not recognized"))
    }
  }

  override def save(expenseSheet: ExpenseSheet): ConnectionIO[Unit] =
    for {
      countEmployees <- sql"select count(*) from employees where id=${expenseSheet.employee.id}".query[Long].unique
      countExpenseSheets <- sql"select count(*) from expensesheets where id=${expenseSheet.id}".query[Long].unique
      insertOrUpdate <-
        if (countEmployees == 0) error(s"Unable to find employee ${expenseSheet.employee.id}")
        else if (countExpenseSheets == 0) insert(expenseSheet)
        else update(expenseSheet)
    } yield insertOrUpdate

  private def update(expenseSheet: ExpenseSheet): ConnectionIO[Unit] =
    sql"""update expensesheets set type=${expenseSheetType(expenseSheet)},
          employeeid=${expenseSheet.employee.id}, expenses=${expenseSheet.expenses}
          where id=${expenseSheet.id}"""
      .update.run.map(_ => ())

  private def insert(expenseSheet: ExpenseSheet): ConnectionIO[Unit] =
    sql"""insert into expensesheets (id, type, employeeid, expenses)
          values (${expenseSheet.id}, ${expenseSheetType(expenseSheet)},
            ${expenseSheet.employee.id}, ${expenseSheet.expenses})"""
    .update.run.map(_ => ())

  private def error(errorMessage: String): ConnectionIO[Unit] =
    ME.raiseError(new IllegalArgumentException(errorMessage))

  private def expenseSheetType(expenseSheet: ExpenseSheet) : ExpenseSheetType = expenseSheet match {
    case OpenExpenseSheet(_, _, _) => "O"
    case ClaimedExpenseSheet(_, _, _) => "C"
  }
}
