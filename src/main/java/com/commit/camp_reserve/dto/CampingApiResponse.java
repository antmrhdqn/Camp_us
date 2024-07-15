package com.commit.camp_reserve.dto;

import lombok.Data;

import java.util.List;

@Data
public class CampingApiResponse {
    private Response response;

    @Data
    public static class Response {
        private Body body;

        @Data
        public static class Body {
            private Items items;

            @Data
            public static class Items {
                private List<CampingDto> item;
            }
        }
    }
}

