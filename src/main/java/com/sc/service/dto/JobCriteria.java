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

/**
 * Criteria class for the {@link com.sc.domain.Job} entity. This class is used
 * in {@link com.sc.web.rest.JobResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /jobs?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class JobCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter jobTitle;

    private LongFilter minSalary;

    private LongFilter maxSalary;

    private LongFilter taskId;

    private LongFilter employeeId;

    public JobCriteria(){
    }

    public JobCriteria(JobCriteria other){
        this.id = other.id == null ? null : other.id.copy();
        this.jobTitle = other.jobTitle == null ? null : other.jobTitle.copy();
        this.minSalary = other.minSalary == null ? null : other.minSalary.copy();
        this.maxSalary = other.maxSalary == null ? null : other.maxSalary.copy();
        this.taskId = other.taskId == null ? null : other.taskId.copy();
        this.employeeId = other.employeeId == null ? null : other.employeeId.copy();
    }

    @Override
    public JobCriteria copy() {
        return new JobCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(StringFilter jobTitle) {
        this.jobTitle = jobTitle;
    }

    public LongFilter getMinSalary() {
        return minSalary;
    }

    public void setMinSalary(LongFilter minSalary) {
        this.minSalary = minSalary;
    }

    public LongFilter getMaxSalary() {
        return maxSalary;
    }

    public void setMaxSalary(LongFilter maxSalary) {
        this.maxSalary = maxSalary;
    }

    public LongFilter getTaskId() {
        return taskId;
    }

    public void setTaskId(LongFilter taskId) {
        this.taskId = taskId;
    }

    public LongFilter getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(LongFilter employeeId) {
        this.employeeId = employeeId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final JobCriteria that = (JobCriteria) o;
        return
            Objects.equals(id, that.id) &&
            Objects.equals(jobTitle, that.jobTitle) &&
            Objects.equals(minSalary, that.minSalary) &&
            Objects.equals(maxSalary, that.maxSalary) &&
            Objects.equals(taskId, that.taskId) &&
            Objects.equals(employeeId, that.employeeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
        id,
        jobTitle,
        minSalary,
        maxSalary,
        taskId,
        employeeId
        );
    }

    @Override
    public String toString() {
        return "JobCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (jobTitle != null ? "jobTitle=" + jobTitle + ", " : "") +
                (minSalary != null ? "minSalary=" + minSalary + ", " : "") +
                (maxSalary != null ? "maxSalary=" + maxSalary + ", " : "") +
                (taskId != null ? "taskId=" + taskId + ", " : "") +
                (employeeId != null ? "employeeId=" + employeeId + ", " : "") +
            "}";
    }

}
