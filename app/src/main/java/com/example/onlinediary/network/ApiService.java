package com.example.onlinediary.network;

import com.example.onlinediary.model.Accrual;
import com.example.onlinediary.model.Assessment;
import com.example.onlinediary.model.DashboardStatsResponse;
import com.example.onlinediary.model.GradeRequest;
import com.example.onlinediary.model.Group;
import com.example.onlinediary.model.GroupCreateRequest;
import com.example.onlinediary.model.HomeworkItem;
import com.example.onlinediary.model.JournalEntry;
import com.example.onlinediary.model.JournalEntryUpdate;
import com.example.onlinediary.model.LoginRequest;
import com.example.onlinediary.model.LoginResponse;
import com.example.onlinediary.model.PasswordUpdateRequest;
import com.example.onlinediary.model.ScheduleEvent;
import com.example.onlinediary.model.ScheduleRequest;
import com.example.onlinediary.model.Subject;
import com.example.onlinediary.model.SubjectAssignmentRequest;
import com.example.onlinediary.model.SubjectRequest;
import com.example.onlinediary.model.SubmissionItem;
import com.example.onlinediary.model.TeacherTask;
import com.example.onlinediary.model.User;
import com.example.onlinediary.model.UserUpdateRequest;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("api/auth/me")
    Call<User> getMe();

    @Multipart
    @POST("api/auth/avatar")
    Call<ResponseBody> updateAvatar(@Part MultipartBody.Part file);

    @POST("api/auth/update-password")
    Call<ResponseBody> updatePassword(@Body PasswordUpdateRequest request);

    @Multipart
    @POST("api/auth/register")
    Call<User> registerUser(@PartMap Map<String, RequestBody> fields,
                            @Part List<MultipartBody.Part> files);

    @GET("api/auth/users/all")
    Call<List<User>> getUsers();

    @GET("api/auth/users/teachers")
    Call<List<User>> getTeachers();

    @GET("api/auth/users/{id}")
    Call<User> getUser(@Path("id") long id);

    @PUT("api/auth/users/{id}")
    Call<User> updateUser(@Path("id") long id, @Body UserUpdateRequest request);

    @DELETE("api/auth/users/{id}")
    Call<ResponseBody> deleteUser(@Path("id") long id);

    @GET("api/groups")
    Call<List<Group>> getGroups();

    @POST("api/groups")
    Call<Group> createGroup(@Body GroupCreateRequest request);

    @DELETE("api/groups/{id}")
    Call<ResponseBody> deleteGroup(@Path("id") long id);

    @GET("api/groups/{groupId}/subjects")
    Call<List<Subject>> getGroupSubjects(@Path("groupId") long groupId);

    @GET("api/subjects")
    Call<List<Subject>> getSubjects();

    @POST("api/subjects")
    Call<Subject> createSubject(@Body SubjectRequest request);

    @PUT("api/subjects/{id}")
    Call<Subject> updateSubject(@Path("id") long id, @Body SubjectRequest request);

    @DELETE("api/subjects/{id}")
    Call<ResponseBody> deleteSubject(@Path("id") long id);

    @POST("api/subjects/assign-teacher")
    Call<ResponseBody> assignTeacher(@Body SubjectAssignmentRequest request);

    @GET("api/subjects/{id}/teachers")
    Call<List<User>> getTeachersBySubject(@Path("id") long subjectId);

    @GET("api/subjects/my")
    Call<List<Subject>> getMySubjects();

    @GET("api/schedule")
    Call<List<ScheduleEvent>> getSchedule(@Query("start") String start,
                                          @Query("end") String end);

    @GET("api/schedule/item/{id}")
    Call<ScheduleEvent> getScheduleItem(@Path("id") long id);

    @POST("api/schedule")
    Call<ScheduleEvent> createSchedule(@Body ScheduleRequest request);

    @PUT("api/schedule/{id}")
    Call<ScheduleEvent> updateSchedule(@Path("id") long id,
                                       @Body ScheduleRequest request);

    @DELETE("api/schedule/{id}")
    Call<ResponseBody> deleteSchedule(@Path("id") long id);

    @Multipart
    @POST("api/schedule/import")
    Call<ResponseBody> importSchedule(@Part MultipartBody.Part file);

    @GET("api/journal/{scheduleId}")
    Call<List<JournalEntry>> getJournal(@Path("scheduleId") long scheduleId);

    @POST("api/journal/{scheduleId}/save")
    Call<ResponseBody> saveJournal(@Path("scheduleId") long scheduleId,
                                   @Body List<JournalEntryUpdate> payload);

    @GET("api/assessments")
    Call<List<HomeworkItem>> getHomeworks();

    @GET("api/assessments/my")
    Call<List<TeacherTask>> getTeacherTasks();

    @GET("api/assessments/{id}")
    Call<Assessment> getAssessment(@Path("id") long id);

    @DELETE("api/assessments/{id}")
    Call<ResponseBody> deleteAssessment(@Path("id") long id);

    @GET("api/assessments/{id}/submissions")
    Call<List<SubmissionItem>> getSubmissions(@Path("id") long id);

    @POST("api/assessments/{id}/submissions/{studentId}/grade")
    Call<ResponseBody> gradeSubmission(@Path("id") long assessmentId,
                                       @Path("studentId") long studentId,
                                       @Body GradeRequest request);

    @DELETE("api/assessments/{id}/submission")
    Call<ResponseBody> deleteSubmission(@Path("id") long id);

    @Multipart
    @POST("api/assessments/{id}/submit")
    Call<ResponseBody> submitHomework(@Path("id") long id,
                                      @Part List<MultipartBody.Part> files,
                                      @Part("comment") RequestBody comment);

    @Multipart
    @POST("api/assessments")
    Call<HomeworkItem> createHomework(@PartMap Map<String, RequestBody> fields,
                                      @Part List<MultipartBody.Part> files);

    @GET("api/student/stats/accruals")
    Call<List<Accrual>> getAccruals();

    @GET("api/student/stats/dashboard")
    Call<DashboardStatsResponse> getDashboardStats(@Query("period") String period);
}
