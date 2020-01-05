package com.misbahulard.util;

import com.misbahulard.model.AuthResponse;
import com.misbahulard.model.HostResponse;
import com.misbahulard.model.SectionResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.Map;

public interface ApiService {
    @POST("user/")
    Call<AuthResponse> authService();

    @GET("sections/{id}/subnets/")
    Call<SectionResponse> sectionService(@HeaderMap Map<String, String> headers, @Path("id") String id);

    @GET("subnets/{id}/addresses/")
    Call<HostResponse> hostService(@HeaderMap Map<String, String> headers, @Path("id") String id);
}
