package Infrastructure.Doobie

import Expenses.Model.Employee
import Expenses.Model.Employee.EmployeeId
import Expenses.Repositories.EmployeeRepository
import Infrastructure.EmployeeRepositoryContractTest
import doobie.free.connection.ConnectionIO
import doobie.postgres.implicits._
import BaseDoobieTest._

class DoobieEmployeeRepositoryTest extends EmployeeRepositoryContractTest[ConnectionIO] with BaseDoobieTest {
  override protected def beforeEach(): Unit = {
    super.beforeEach()
    setUpDatabase()
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    cleanUpDatabase()
  }

  override def createRepositoryWith(employees: List[Employee]): EmployeeRepository[ConnectionIO] =
    createRepositoriesWith(List(), employees)._2

  override def cleanUp(employeeIds: List[EmployeeId]): Unit = cleanUp(List(), employeeIds)
}
