package org.p10.PetStore.Models;

import com.google.gson.annotations.SerializedName;

public enum OrderStatus {
    @SerializedName("0")
    Placed,
    @SerializedName("1")
    Processing,
    @SerializedName("2")
    Shipped
}
