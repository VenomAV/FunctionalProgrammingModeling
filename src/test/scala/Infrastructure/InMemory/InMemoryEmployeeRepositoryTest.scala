package Infrastructure.InMemory

import Expenses.Model.{Employee, EmployeeId}
import Expenses.Repositories.EmployeeRepository
import Expenses.TestUtils.AcceptanceTestUtils.{Test, TestState}
import Expenses.TestUtils.InMemoryEmployeeRepository
import Infrastructure.EmployeeRepositoryContractTest
import cats.implicits._

class InMemoryEmployeeRepositoryTest extends EmployeeRepositoryContractTest[Test] {
  implicit var state : TestState = _

  override def createRepositoryWith(employees: List[Employee]): EmployeeRepository[Test] = {
    state = TestState(
      employees,
      List(),
      List())
    new InMemoryEmployeeRepository
  }

  override def run[A](toBeExecuted: Test[A]) : Either[Throwable, A] =
    toBeExecuted.runA(state)

  override def cleanUp(employeeIds: List[EmployeeId]): Unit = ()
}