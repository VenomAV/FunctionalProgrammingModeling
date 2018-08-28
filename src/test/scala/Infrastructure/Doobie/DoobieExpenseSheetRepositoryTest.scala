package Infrastructure.Doobie

import Expenses.Model.ExpenseSheet.ExpenseSheetId
import Expenses.Model.{Employee, ExpenseSheet}
import Expenses.Repositories.ExpenseSheetRepository
import Infrastructure.ExpenseSheetRepositoryContractTest
import doobie.free.connection.ConnectionIO
import doobie.postgres.implicits._
import BaseDoobieTest._

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
}
