package Infrastructure.Repositories

import Expenses.Model.ExpenseSheet.ExpenseSheetId
import Expenses.Model._
import Expenses.Repositories.ExpenseSheetRepository
import Infrastructure.Repositories.Doobie.implicits._
import cats.free.Free
import doobie.free.connection.{ConnectionIO, ConnectionOp}
import doobie.implicits._
import doobie.postgres.implicits._

class DoobieExpenseSheetRepository extends ExpenseSheetRepository[ConnectionIO] {

  type ExpenseSheetType = String
  type DBTuple = (ExpenseSheetId, ExpenseSheetType, List[Expense], Employee)

  override def get(id: ExpenseSheetId): ConnectionIO[Option[ExpenseSheet]] =
    select(id)
      .map(_.map(unsafeDBTupleToExpenseSheet))

  private def select(id: ExpenseSheetId): ConnectionIO[Option[DBTuple]] =
    sql"""select es.id, es.type, es.expenses, e.id, e.name, e.surname
          from expensesheets es
          join employees e on e.id = es.employeeid
          where es.id=$id"""
      .query[DBTuple]
      .option

  private def unsafeDBTupleToExpenseSheet(tuple: DBTuple) : ExpenseSheet = {
    val (id, expenseSheetType, expenses, employee) = tuple

    expenseSheetType match {
      case "O" => OpenExpenseSheet(id, employee, expenses)
      case "C" => ClaimedExpenseSheet(id, employee, expenses)
      case _ => throw new UnsupportedOperationException
    }
  }

  override def save(expenseSheet: ExpenseSheet): ConnectionIO[Unit] =
    (for {
      countEmployees <- sql"select count(*) from employees where id=${expenseSheet.employee.id}".query[Long].unique
      countExpenseSheets <- sql"select count(*) from expensesheets where id=${expenseSheet.id}".query[Long].unique
      insertOrUpdate <-
        if (countEmployees == 0) Free.pure[ConnectionOp, Int](0)
        else if (countExpenseSheets == 0) insert(expenseSheet)
        else update(expenseSheet)
    } yield insertOrUpdate).map(_ => ())

  private def update(expenseSheet: ExpenseSheet): ConnectionIO[Int] =
    sql"""update expensesheets set type=${expenseSheetType(expenseSheet)},
          employeeid=${expenseSheet.employee.id}, expenses=${expenseSheet.expenses}
          where id=${expenseSheet.id}"""
      .update.run

  private def insert(expenseSheet: ExpenseSheet): ConnectionIO[Int] =
    sql"""insert into expensesheets (id, type, employeeid, expenses)
          values (${expenseSheet.id}, ${expenseSheetType(expenseSheet)},
            ${expenseSheet.employee.id}, ${expenseSheet.expenses})"""
    .update.run

  private def expenseSheetType(expenseSheet: ExpenseSheet) : ExpenseSheetType = expenseSheet match {
    case OpenExpenseSheet(_, _, _) => "O"
    case ClaimedExpenseSheet(_, _, _) => "C"
    case _ => throw new UnsupportedOperationException
  }
}
