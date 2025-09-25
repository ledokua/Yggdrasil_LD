package net.ledok.prime.api;

import net.ledok.prime.api.dto.PrimeStatusDTO;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import java.util.UUID;

public interface MinestarApi {

    String API_URL = "https://api.minestar.com.ua";

    static MinestarApi create() {
        return new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MinestarApi.class);
    }

    static String formatUUID(UUID uuid) {
        return uuid.toString().replace("-", "");
    }

    @GET("/prime/status/{uuid}")
    Call<PrimeStatusDTO> getPrimeStatusByUuid(@Path("uuid") String uuid);
}
