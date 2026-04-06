package com.library.borrowservice.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BookServiceClient {

    private final RestClient bookServiceClient;

    public BookServiceClient(@Qualifier("bookServiceClient") RestClient bookServiceClient) {
        this.bookServiceClient = bookServiceClient;
    }

    public CopyInfo getCopyByBarcode(String barcode, String authorizationHeader) {
        var req = bookServiceClient.get().uri("/api/v1/copies/barcode/{barcode}", barcode);
        if (authorizationHeader != null && !authorizationHeader.isBlank()) {
            req = req.header(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
        return req.retrieve().body(CopyInfo.class);
    }

    public void reserve(String barcode, String authorizationHeader) {
        var req = bookServiceClient.post().uri("/api/v1/copies/barcode/{barcode}/reserve", barcode);
        if (authorizationHeader != null && !authorizationHeader.isBlank()) {
            req = req.header(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
        req.retrieve().toBodilessEntity();
    }

    public void releaseReservation(String barcode, String authorizationHeader) {
        var req = bookServiceClient.post().uri("/api/v1/copies/barcode/{barcode}/release-reservation", barcode);
        if (authorizationHeader != null && !authorizationHeader.isBlank()) {
            req = req.header(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
        req.retrieve().toBodilessEntity();
    }
}
