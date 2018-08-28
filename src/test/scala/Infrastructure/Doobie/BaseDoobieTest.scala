package Infrastructure.Doobie

import Expenses.Model.Employee.EmployeeId
import Expenses.Model.ExpenseSheet.ExpenseSheetId
import Expenses.Model.{Employee, ExpenseSheet}
import Expenses.Repositories.{EmployeeRepository, ExpenseSheetRepository}
import Infrastructure.Repositories.{DoobieEmployeeRepository, DoobieExpenseSheetRepository}
import cats.effect.{Async, IO}
import doobie.free.connection.ConnectionIO
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import doobie.implicits._
import doobie.postgres.implicits._

object BaseDoobieTest {
  implicit val M = Async[ConnectionIO]
}

trait BaseDoobieTest {
  implicit var xa: Aux[IO, Unit] = _
  var employeeIds: List[EmployeeId] = _
  var expenseSheetIds: List[ExpenseSheetId] = _

  def setUpDatabase(): Unit = {
    xa = Transactor.fromDriverManager[IO](
      "org.postgresql.Driver",
      "jdbc:postgresql:postgres",
      "postgres",
      "p4ssw0r#"
    )
  }

  def cleanUpDatabase(): Unit = {
    cleanUp(expenseSheetIds, employeeIds)
  }

  def cleanUp(expenseSheetIds: List[ExpenseSheetId], employeeIds: List[EmployeeId]): Unit = {
    expenseSheetIds.map(x => sql"delete from expensesheets where id=$x".update.run.transact(xa).unsafeRunSync)
    employeeIds.map(x => sql"delete from employees where id=$x".update.run.transact(xa).unsafeRunSync)
  }

  def createRepositoriesWith(expenseSheets: List[ExpenseSheet], employees: List[Employee]):
  (ExpenseSheetRepository[ConnectionIO], EmployeeRepository[ConnectionIO]) = {
    val employeeRepository = new DoobieEmployeeRepository
    val expenseSheetRepository = new DoobieExpenseSheetRepository()

    employeeIds = employees.map(_.id)
    expenseSheetIds = expenseSheets.map(_.id)

    employees
      .foreach(employeeRepository.save(_).transact(xa).unsafeRunSync())
    expenseSheets
      .foreach(expenseSheetRepository.save(_).transact(xa).unsafeRunSync())
    (expenseSheetRepository, employeeRepository)
  }

  def run[A](toBeExecuted: ConnectionIO[A]): A = toBeExecuted.transact(xa).unsafeRunSync

  def existExpenseSheet(id: ExpenseSheetId): ConnectionIO[Boolean] =
    sql"select 1 from expensesheets where id=$id"
      .query[Int]
      .option
      .map({
        case None => false
        case _ => true
      })
}
