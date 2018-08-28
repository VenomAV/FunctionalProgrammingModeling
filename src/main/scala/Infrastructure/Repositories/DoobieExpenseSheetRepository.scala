package Infrastructure.Repositories

import Expenses.Model.ExpenseSheet.ExpenseSheetId
import Expenses.Model._
import Expenses.Repositories.ExpenseSheetRepository
import Expenses.Utils.ErrorManagement.{ApplicationResult, ErrorList}
import Expenses.Utils.ErrorManagement.implicits._
import Infrastructure.Repositories.Doobie.implicits._
import cats.free.Free
import cats.implicits._
import doobie.free.connection.{ConnectionIO, ConnectionOp}
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.invariant.UnexpectedEnd

class DoobieExpenseSheetRepository extends ExpenseSheetRepository[ConnectionIO] {

  type ExpenseSheetType = String
  type DBTuple = (ExpenseSheetId, ExpenseSheetType, List[Expense], Employee)

  override def get(id: ExpenseSheetId): ConnectionIO[ApplicationResult[ExpenseSheet]] =
    sql"""select es.id, es.type, es.expenses, e.id, e.name, e.surname
          from expensesheets es
          join employees e on e.id = es.employeeid
          where es.id=$id"""
      .query[DBTuple]
      .unique
      .attempt
      .map(_.leftMap({
        case UnexpectedEnd => ErrorList.of(s"Unable to find expense sheet $id")
        case x => x.toError
      }).flatMap(unsafeDBTupleToExpenseSheet))

  private def unsafeDBTupleToExpenseSheet(tuple: DBTuple) : ApplicationResult[ExpenseSheet] = {
    val (id, expenseSheetType, expenses, employee) = tuple

    expenseSheetType match {
      case "O" => Right(OpenExpenseSheet(id, employee, expenses))
      case "C" => Right(ClaimedExpenseSheet(id, employee, expenses))
      case _ => Left(ErrorList.of("Expense sheet type not recognized"))
    }
  }

  override def save(expenseSheet: ExpenseSheet): ConnectionIO[ApplicationResult[Unit]] =
    (for {
      countEmployees <- sql"select count(*) from employees where id=${expenseSheet.employee.id}".query[Long].unique
      countExpenseSheets <- sql"select count(*) from expensesheets where id=${expenseSheet.id}".query[Long].unique
      insertOrUpdate <-
        if (countEmployees == 0) error(s"Unable to find employee ${expenseSheet.employee.id}")
        else if (countExpenseSheets == 0) insert(expenseSheet)
        else update(expenseSheet)
    } yield insertOrUpdate).map(_.map(_ =>()).leftMap(_.toError))

  private def update(expenseSheet: ExpenseSheet): ConnectionIO[Either[Throwable, Int]] =
    sql"""update expensesheets set type=${expenseSheetType(expenseSheet)},
          employeeid=${expenseSheet.employee.id}, expenses=${expenseSheet.expenses}
          where id=${expenseSheet.id}"""
      .update.run.attempt

  private def insert(expenseSheet: ExpenseSheet): ConnectionIO[Either[Throwable, Int]] =
    sql"""insert into expensesheets (id, type, employeeid, expenses)
          values (${expenseSheet.id}, ${expenseSheetType(expenseSheet)},
            ${expenseSheet.employee.id}, ${expenseSheet.expenses})"""
    .update.run.attempt

  private def error(errorMessage: String): ConnectionIO[Either[Throwable, Int]] =
    Free.pure[ConnectionOp, Either[Throwable, Int]](Left(new IllegalArgumentException(errorMessage)))

  private def expenseSheetType(expenseSheet: ExpenseSheet) : ExpenseSheetType = expenseSheet match {
    case OpenExpenseSheet(_, _, _) => "O"
    case ClaimedExpenseSheet(_, _, _) => "C"
  }
}
