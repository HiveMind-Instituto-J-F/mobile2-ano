package com.aula.mobile_hivemind.api;

import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://api-mongo-kmyg.onrender.com";
    private static final String SQL_BASE_URL = "https://api-2-0mqv.onrender.com";
    private static Retrofit retrofit = null;
    private static Retrofit sqlRetrofit = null;
    private static SqlApiService sqlApiService = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(new BasicAuthInterceptor("admin", "teste"))
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build();

            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    .registerTypeAdapter(Date.class, new DateSerializer())
                    .registerTypeAdapter(Date.class, new DateDeserializer())
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit.create(ApiService.class);
    }

    public static SqlApiService getSqlApiService() {
        if (sqlApiService == null) {
            // Criar credenciais Basic Auth
            String credentials = "admin" + ":" + "teste";
            final String basic = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request.Builder requestBuilder = original.newBuilder()
                                .header("Authorization", basic)
                                .header("Accept", "application/json")
                                .method(original.method(), original.body());

                        Request request = requestBuilder.build();
                        return chain.proceed(request);
                    })
                    .build();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .registerTypeAdapter(java.sql.Date.class, (JsonDeserializer<java.sql.Date>) (json, typeOfT, context) -> {
                        String value = json.getAsString();
                        List<String> dateFormats = Arrays.asList(
                                "yyyy-MM-dd",
                                "MMM dd, yyyy",
                                "MMM dd, yyyy",
                                "LLL dd, yyyy"
                        );
                        for (String format : dateFormats) {
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                                Date utilDate = sdf.parse(value);
                                return new java.sql.Date(utilDate.getTime());
                            } catch (Exception ignored) {}
                        }
                        throw new JsonParseException("Formato de data inválido: " + value);
                    })
                    .registerTypeAdapter(java.sql.Time.class, (JsonDeserializer<java.sql.Time>) (json, typeOfT, context) -> {
                        String value = json.getAsString();
                        List<String> timeFormats = Arrays.asList(
                                "HH:mm:ss",     // 14:08:00
                                "hh:mm:ss a",   // 02:08:00 PM
                                "hh:mm a"       // 02:08 PM
                        );
                        for (String format : timeFormats) {
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                                Date utilDate = sdf.parse(value);
                                return new Time(utilDate.getTime());
                            } catch (Exception ignored) {}
                        }
                        throw new JsonParseException("Formato de hora inválido: " + value);
                    })
                    .registerTypeAdapter(java.sql.Date.class, (JsonSerializer<java.sql.Date>) (src, type, context) -> {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                        return new JsonPrimitive(sdf.format(src));
                    })
                    .registerTypeAdapter(java.sql.Time.class, (JsonSerializer<Time>) (src, type, context) -> {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.US);
                        return new JsonPrimitive(sdf.format(src));
                    })
                    .registerTypeAdapter(Date.class, new DateSerializer())
                    .registerTypeAdapter(Date.class, new DateDeserializer())
                    .create();

            sqlRetrofit = new Retrofit.Builder()
                    .baseUrl(SQL_BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            sqlApiService = sqlRetrofit.create(SqlApiService.class);
        }
        return sqlApiService;
    }

    private static class DateSerializer implements JsonSerializer<Date> {
        @Override
        public JsonElement serialize(Date date, Type typeOfSrc, JsonSerializationContext context) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                return new JsonPrimitive(sdf.format(date));
            } catch (Exception e) {
                Log.e("DateSerializer", "Erro ao serializar data: " + date, e);
                return new JsonPrimitive("");
            }
        }
    }

    private static class DateDeserializer implements JsonDeserializer<Date> {
        private final List<String> dateFormats = Arrays.asList(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "EEE, dd MMM yyyy HH:mm:ss zzz",
                "yyyy-MM-dd",
                "dd, MMM yyyy HH:mm"
        );

        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            for (String format : dateFormats) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    return sdf.parse(json.getAsString());
                } catch (ParseException ignored) {
                }
            }
            throw new JsonParseException("Não foi possível parsear a data: " + json.getAsString());
        }
    }
}