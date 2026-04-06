package com.library.notificationservice.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class BorrowServiceClient {

    private final RestClient borrowServiceClient;

    public BorrowServiceClient(RestClient borrowServiceClient) {
        this.borrowServiceClient = borrowServiceClient;
    }

    public List<BorrowOverdueResponse> getOverdueBorrows() {
        try {
            BorrowOverdueResponse[] res =
                    borrowServiceClient
                            .get()
                            .uri("/api/v1/borrows/overdue")
                            .retrieve()
                            .body(BorrowOverdueResponse[].class);
            if (res == null) {
                return Collections.emptyList();
            }
            return Arrays.asList(res);
        } catch (RestClientException ex) {
            return Collections.emptyList();
        }
    }
}

