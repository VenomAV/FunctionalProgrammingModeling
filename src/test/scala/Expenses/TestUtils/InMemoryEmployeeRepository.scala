package Expenses.TestUtils

import Expenses.Model.{Employee, EmployeeId}
import Expenses.Repositories.EmployeeRepository
import Expenses.TestUtils.AcceptanceTestUtils.{OrError, Test, TestState}
import cats.data.StateT
import cats.data.StateT._
import cats.implicits._

class InMemoryEmployeeRepository extends EmployeeRepository[Test] {
  override def get(id: EmployeeId): Test[Employee] =
    for {
      state <- StateT.get[OrError, TestState]
      result <- state.employees.find(_.id == id) match {
        case Some(x) => pure[OrError, TestState, Employee](x)
        case _ => liftF[OrError, TestState, Employee](Left(new Error(s"Unable to find employee $id")))
      }
    } yield result

  override def save(employee: Employee): Test[Unit] =
    for {
      _ <- modify[OrError, TestState](state => state.copy(employees = employee :: state.employees))
    } yield ()
}