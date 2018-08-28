package Expenses.TestUtils

import Expenses.Model.Employee
import Expenses.Model.Employee.EmployeeId
import Expenses.Repositories.EmployeeRepository
import Expenses.TestUtils.AcceptanceTestUtils.Test
import Expenses.Utils.ErrorManagement.ApplicationResult
import Expenses.Utils.ErrorManagement.implicits._
import cats.data.State

class InMemoryEmployeeRepository extends EmployeeRepository[Test] {
  override def get(id: EmployeeId): Test[ApplicationResult[Employee]] =
    State {
      state => (state, state.employees.find(_.id == id).orError(s"Unable to find employee $id"))
    }

  override def save(employee: Employee): Test[ApplicationResult[Unit]] =
    State {
      state => (state.copy(employees = employee :: state.employees), Right(()))
    }
}