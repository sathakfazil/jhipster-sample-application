package com.sc.service.dto;

import java.io.Serializable;
import java.util.Objects;
import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.DoubleFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.FloatFilter;
import io.github.jhipster.service.filter.IntegerFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import io.github.jhipster.service.filter.InstantFilter;

/**
 * Criteria class for the {@link com.sc.domain.Employee} entity. This class is used
 * in {@link com.sc.web.rest.EmployeeResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /employees?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class EmployeeCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter firstName;

    private StringFilter lastName;

    private StringFilter email;

    private StringFilter phoneNumber;

    private InstantFilter hireDate;

    private LongFilter salary;

    private LongFilter commissionPct;

    private LongFilter jobId;

    private LongFilter managerId;

    private LongFilter departmentId;

    public EmployeeCriteria(){
    }

    public EmployeeCriteria(EmployeeCriteria other){
        this.id = other.id == null ? null : other.id.copy();
        this.firstName = other.firstName == null ? null : other.firstName.copy();
        this.lastName = other.lastName == null ? null : other.lastName.copy();
        this.email = other.email == null ? null : other.email.copy();
        this.phoneNumber = other.phoneNumber == null ? null : other.phoneNumber.copy();
        this.hireDate = other.hireDate == null ? null : other.hireDate.copy();
        this.salary = other.salary == null ? null : other.salary.copy();
        this.commissionPct = other.commissionPct == null ? null : other.commissionPct.copy();
        this.jobId = other.jobId == null ? null : other.jobId.copy();
        this.managerId = other.managerId == null ? null : other.managerId.copy();
        this.departmentId = other.departmentId == null ? null : other.departmentId.copy();
    }

    @Override
    public EmployeeCriteria copy() {
        return new EmployeeCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getFirstName() {
        return firstName;
    }

    public void setFirstName(StringFilter firstName) {
        this.firstName = firstName;
    }

    public StringFilter getLastName() {
        return lastName;
    }

    public void setLastName(StringFilter lastName) {
        this.lastName = lastName;
    }

    public StringFilter getEmail() {
        return email;
    }

    public void setEmail(StringFilter email) {
        this.email = email;
    }

    public StringFilter getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(StringFilter phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public InstantFilter getHireDate() {
        return hireDate;
    }

    public void setHireDate(InstantFilter hireDate) {
        this.hireDate = hireDate;
    }

    public LongFilter getSalary() {
        return salary;
    }

    public void setSalary(LongFilter salary) {
        this.salary = salary;
    }

    public LongFilter getCommissionPct() {
        return commissionPct;
    }

    public void setCommissionPct(LongFilter commissionPct) {
        this.commissionPct = commissionPct;
    }

    public LongFilter getJobId() {
        return jobId;
    }

    public void setJobId(LongFilter jobId) {
        this.jobId = jobId;
    }

    public LongFilter getManagerId() {
        return managerId;
    }

    public void setManagerId(LongFilter managerId) {
        this.managerId = managerId;
    }

    public LongFilter getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(LongFilter departmentId) {
        this.departmentId = departmentId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final EmployeeCriteria that = (EmployeeCriteria) o;
        return
            Objects.equals(id, that.id) &&
            Objects.equals(firstName, that.firstName) &&
            Objects.equals(lastName, that.lastName) &&
            Objects.equals(email, that.email) &&
            Objects.equals(phoneNumber, that.phoneNumber) &&
            Objects.equals(hireDate, that.hireDate) &&
            Objects.equals(salary, that.salary) &&
            Objects.equals(commissionPct, that.commissionPct) &&
            Objects.equals(jobId, that.jobId) &&
            Objects.equals(managerId, that.managerId) &&
            Objects.equals(departmentId, that.departmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
        id,
        firstName,
        lastName,
        email,
        phoneNumber,
        hireDate,
        salary,
        commissionPct,
        jobId,
        managerId,
        departmentId
        );
    }

    @Override
    public String toString() {
        return "EmployeeCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (firstName != null ? "firstName=" + firstName + ", " : "") +
                (lastName != null ? "lastName=" + lastName + ", " : "") +
                (email != null ? "email=" + email + ", " : "") +
                (phoneNumber != null ? "phoneNumber=" + phoneNumber + ", " : "") +
                (hireDate != null ? "hireDate=" + hireDate + ", " : "") +
                (salary != null ? "salary=" + salary + ", " : "") +
                (commissionPct != null ? "commissionPct=" + commissionPct + ", " : "") +
                (jobId != null ? "jobId=" + jobId + ", " : "") +
                (managerId != null ? "managerId=" + managerId + ", " : "") +
                (departmentId != null ? "departmentId=" + departmentId + ", " : "") +
            "}";
    }

}
