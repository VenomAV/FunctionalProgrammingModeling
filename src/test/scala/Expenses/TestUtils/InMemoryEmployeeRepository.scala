package Expenses.TestUtils

import Expenses.Model.Employee
import Expenses.Model.Employee.EmployeeId
import Expenses.Repositories.EmployeeRepository
import Expenses.TestUtils.AcceptanceTestUtils.Test
import cats.data.State

class InMemoryEmployeeRepository extends EmployeeRepository[Test] {
  override def get(id: EmployeeId): Test[Option[Employee]] =
    State.get.map(_.employees.find(_.id == id))

  override def save(employee: Employee): Test[Unit] =
    State {
      state => (state.copy(employees = employee :: state.employees), ())
    }
}