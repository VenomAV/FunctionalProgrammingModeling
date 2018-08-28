package Infrastructure.InMemory

import Expenses.Model.Employee
import Expenses.Model.Employee.EmployeeId
import Expenses.Repositories.EmployeeRepository
import Expenses.TestUtils.AcceptanceTestUtils.{Test, TestState}
import Expenses.TestUtils.InMemoryEmployeeRepository
import Infrastructure.EmployeeRepositoryContractTest
import org.scalatest.BeforeAndAfter

class InMemoryEmployeeRepositoryTest extends EmployeeRepositoryContractTest[Test] with BeforeAndAfter{
  implicit var state : TestState = _

  before {
    state = TestState(
      List(),
      List(),
      List())
  }

  override def createRepositoryWith(employees: List[Employee]): EmployeeRepository[Test] = {
    state = TestState(
      employees,
      List(),
      List())
    new InMemoryEmployeeRepository
  }

  override def run[A](executionUnit: Test[A]): A = executionUnit.runA(state).value

  override def cleanUp(employeeIds: List[EmployeeId]): Unit = ()
}