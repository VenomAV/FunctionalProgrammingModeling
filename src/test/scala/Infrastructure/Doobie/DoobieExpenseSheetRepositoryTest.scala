package Infrastructure.Doobie

import java.util.UUID

import Expenses.Model.ExpenseSheet.ExpenseSheetId
import Expenses.Model.{Employee, Expense, ExpenseSheet}
import Expenses.Repositories.ExpenseSheetRepository
import Infrastructure.ExpenseSheetRepositoryContractTest
import cats.data.NonEmptyList
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.postgres.implicits._
import Infrastructure.Repositories.Doobie.implicits._

class DoobieExpenseSheetRepositoryTest extends ExpenseSheetRepositoryContractTest[ConnectionIO] with BaseDoobieTest {

  override protected def beforeEach(): Unit =  {
    super.beforeEach()
    setUpDatabase()
  }

  override def afterEach(): Unit = {
    super.afterEach()
    cleanUpDatabase()
  }

  override def createRepositoryWith(expenseSheets: List[ExpenseSheet], employees: List[Employee]):
      ExpenseSheetRepository[ConnectionIO] = createRepositoriesWith(expenseSheets, employees)._1

  override def cleanUp(expenseSheetIds: List[ExpenseSheetId]): Unit = cleanUp(expenseSheetIds, List())

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

      run(sut.get(id)) should be(Left(NonEmptyList("Expense sheet type not recognized", List())))
    }
  }
}
