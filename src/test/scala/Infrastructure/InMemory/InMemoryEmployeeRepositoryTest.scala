package Infrastructure.InMemory

import Expenses.Model.Employee
import Expenses.Model.Employee.EmployeeId
import Expenses.Repositories.{EmployeeRepository, EmployeeRepositoryME}
import Expenses.TestUtils.AcceptanceTestUtils.{Test, TestME, TestState}
import Expenses.TestUtils.{InMemoryEmployeeMERepository, InMemoryEmployeeRepository}
import Infrastructure.{EmployeeRepositoryContractTest, EmployeeRepositoryMEContractTest}
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

  override def run[A](executionUnit: Test[A]): A = executionUnit.runA(state).value

  override def cleanUp(employeeIds: List[EmployeeId]): Unit = ()
}

class InMemoryEmployeeRepositoryMETest extends EmployeeRepositoryMEContractTest[TestME] {
  implicit var state : TestState = _

  override def createRepositoryWith(employees: List[Employee]): EmployeeRepositoryME[TestME] = {
    state = TestState(
      employees,
      List(),
      List())
    new InMemoryEmployeeMERepository
  }

  override def run[A](toBeExecuted: TestME[A]) : Either[Throwable, A] =
    toBeExecuted.runA(state)

  override def cleanUp(employeeIds: List[EmployeeId]): Unit = ()
}