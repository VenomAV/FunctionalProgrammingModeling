package Infrastructure.Doobie

import java.util.UUID

import Expenses.Model.Employee.implicits._
import Expenses.Model.ExpenseSheet.implicits._
import Expenses.Model._
import Expenses.Repositories.ExpenseSheetRepository
import Infrastructure.ExpenseSheetRepositoryContractTest
import Infrastructure.Repositories.Doobie.implicits._
import Infrastructure.Repositories.{DoobieEmployeeRepository, DoobieExpenseSheetRepository}
import cats.effect.IO
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux

class DoobieExpenseSheetRepositoryTest extends ExpenseSheetRepositoryContractTest[ConnectionIO] {
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
  override protected def beforeEach(): Unit = {
    super.beforeEach()
    setUpDatabase()
  }

  override def afterEach(): Unit = {
    super.afterEach()
    cleanUp(expenseSheetIds)
    employeeIds.map(deleteEmployee)
  }

  override def createRepositoryWith(expenseSheets: List[ExpenseSheet], employees: List[Employee]):
      ExpenseSheetRepository[ConnectionIO] = {
    val employeeRepository = new DoobieEmployeeRepository
    val expenseSheetRepository = new DoobieExpenseSheetRepository()

    employeeIds = employees.map(_.id)
    expenseSheetIds = expenseSheets.map(_.id)

    employees
      .foreach(employeeRepository.save(_).transact(xa).unsafeRunSync())
    expenseSheets
      .foreach(expenseSheetRepository.save(_).transact(xa).unsafeRunSync())

    expenseSheetRepository
  }

  override def cleanUp(expenseSheetIds: List[ExpenseSheetId]): Unit = {
    expenseSheetIds.map(deleteExpenseSheet)
  }

  describe("get corner case") {
    it("should fail when expense sheet type is not recognized") {
      val employee = Employee(UUID.randomUUID(), "A", "V")
      val sut = createRepositoryWith(List(), List(employee))
      val id = UUID.randomUUID()
      val esType = "X"

      sql"""insert into expensesheets (id, type, employeeid, expenses)
          values ($id, $esType, ${employee.id}, ${List[Expense]()})"""
        .update.run.transact(xa).unsafeRunSync()

      expenseSheetIds = id :: expenseSheetIds

      run(sut.get(id)) should matchPattern {
        case Left(x : Throwable) if x.getMessage.equals("Expense sheet type not recognized") =>
      }
    }
  }

  def deleteEmployee(id: EmployeeId) : Int = sql"delete from employees where id=$id".update.run.transact(xa).unsafeRunSync

  def deleteExpenseSheet(id: ExpenseSheetId) : Int = sql"delete from expensesheets where id=$id".update.run.transact(xa).unsafeRunSync

  override def run[A](toBeExecuted: ConnectionIO[A]): Either[Throwable, A] =
    toBeExecuted.transact(xa).attempt.unsafeRunSync()
}
