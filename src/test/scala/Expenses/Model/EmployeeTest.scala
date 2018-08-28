package Expenses.Model

import cats.data.NonEmptyList
import cats.data.Validated.{Invalid, Valid}
import org.scalatest.{FunSpec, Matchers}

class EmployeeTest extends FunSpec with Matchers {
  describe("create") {
    it("should return invalid when surname is empty") {
      Employee.create("Andrea", "") should matchPattern {
        case Invalid(NonEmptyList("surname is empty", _)) =>
      }
    }
    it("should return invalid when name is empty") {
      Employee.create("", "Vallotti") should matchPattern {
        case Invalid(NonEmptyList("name is empty", _)) =>
      }
    }
    it("should return an employee") {
      Employee.create("Andrea", "Vallotti") should matchPattern {
        case Valid(Employee(_, "Andrea", "Vallotti")) =>
      }
    }
  }
}
