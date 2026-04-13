package com.aem.playground.core.services.impl;

import com.aem.playground.core.services.MSBookingsService;
import com.aem.playground.core.services.MSBookingsService.BookableService;
import com.aem.playground.core.services.MSBookingsService.BookingRequest;
import com.aem.playground.core.services.MSBookingsService.BookingConfirmation;
import com.aem.playground.core.services.MSBookingsService.Branch;
import com.aem.playground.core.services.MSBookingsService.TimeSlot;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component(service = MSBookingsService.class)
public class MSBookingsServiceImpl implements MSBookingsService {

    private static final Logger log = LoggerFactory.getLogger(MSBookingsServiceImpl.class);

    private static final String MS_GRAPH_API = "https://graph.microsoft.com/v1.0";
    private static final String BOOKING_BUSINESS_ID = "YOUR_BOOKING_BUSINESS_ID";

    private final ObjectMapper mapper = new ObjectMapper();
    private String accessToken;

    @Activate
    protected void activate() {
        log.info("MSBookingsServiceImpl activated");
    }

    public void setAccessToken(String token) {
        this.accessToken = token;
    }

    @Override
    public List<BookableService> getServices() {
        List<BookableService> services = new ArrayList<>();
        try {
            String url = MS_GRAPH_API + "/solutions/bookingBusinesses/" + BOOKING_BUSINESS_ID + "/services";
            JsonNode response = callMSGraph(url);
            if (response != null && response.has("value")) {
                ArrayNode servicesArray = (ArrayNode) response.get("value");
                for (JsonNode node : servicesArray) {
                    final String id = node.path("id").asText();
                    final String displayName = node.path("displayName").asText();
                    final String description = node.path("description").asText();
                    final int duration = node.path("duration").asInt(60) / 60;
                    final double price = node.path("price").asDouble(0);
                    services.add(new BookableService() {
                        @Override
                        public String getId() { return id; }
                        @Override
                        public String getDisplayName() { return displayName; }
                        @Override
                        public String getDescription() { return description; }
                        @Override
                        public int getDurationMinutes() { return duration; }
                        @Override
                        public double getPrice() { return price; }
                    });
                }
            }
        } catch (Exception e) {
            log.error("Error fetching services from MS Bookings", e);
        }
        return services;
    }

    @Override
    public List<Branch> getBranches() {
        List<Branch> branches = new ArrayList<>();
        try {
            String url = MS_GRAPH_API + "/solutions/bookingBusinesses/" + BOOKING_BUSINESS_ID + "/staff";
            JsonNode response = callMSGraph(url);
            if (response != null && response.has("value")) {
                ArrayNode staffArray = (ArrayNode) response.get("value");
                for (JsonNode node : staffArray) {
                    final String id = node.path("id").asText();
                    final String displayName = node.path("displayName").asText();
                    final String address = node.path("address").path("street").asText("");
                    final String city = node.path("address").path("city").asText("");
                    branches.add(new Branch() {
                        @Override
                        public String getId() { return id; }
                        @Override
                        public String getDisplayName() { return displayName; }
                        @Override
                        public String getAddress() { return address; }
                        @Override
                        public String getCity() { return city; }
                    });
                }
            }
        } catch (Exception e) {
            log.error("Error fetching branches from MS Bookings", e);
        }
        return branches;
    }

    @Override
    public List<TimeSlot> getAvailableSlots(String serviceId, String branchId, String date) {
        List<TimeSlot> slots = new ArrayList<>();
        try {
            String url = MS_GRAPH_API + "/solutions/bookingBusinesses/" + BOOKING_BUSINESS_ID
                    + "/appointments?$filter=serviceId eq '" + serviceId + "' and date eq '" + date + "'";
            JsonNode response = callMSGraph(url);
            if (response != null && response.has("value")) {
                ArrayNode appointments = (ArrayNode) response.get("value");
                for (JsonNode appt : appointments) {
                    final String startTime = appt.path("startDateTime").asText();
                    final String endTime = appt.path("endDateTime").asText();
                    final String assignedStaff = appt.path("staffIds").isArray() && appt.path("staffIds").size() > 0
                            ? appt.path("staffIds").get(0).asText() : "";
                    final boolean isAvailable = appt.path("isConfirmed").asBoolean(true);
                    if (assignedStaff.equals(branchId) || branchId.isEmpty()) {
                        slots.add(new TimeSlot() {
                            @Override
                            public String getStartTime() { return startTime; }
                            @Override
                            public String getEndTime() { return endTime; }
                            @Override
                            public String getBranchId() { return assignedStaff; }
                            @Override
                            public boolean isAvailable() { return isAvailable; }
                        });
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching slots from MS Bookings", e);
        }
        return slots;
    }

    @Override
    public BookingConfirmation bookAppointment(BookingRequest request) {
        try {
            String url = MS_GRAPH_API + "/solutions/bookingBusinesses/" + BOOKING_BUSINESS_ID + "/appointments";
            ObjectNode body = mapper.createObjectNode();
            body.put("serviceId", request.getServiceId());
            body.putArray("staffIds").add(request.getBranchId());
            body.put("startDateTime", request.getSlotStartTime());
            body.put("customerName", request.getCustomerName());
            body.put("customerEmail", request.getCustomerEmail());
            body.put("customerPhone", request.getCustomerPhone());
            if (StringUtils.isNotBlank(request.getNotes())) {
                body.put("notes", request.getNotes());
            }
            JsonNode response = callMSGraphPost(url, body);
            if (response != null) {
                final String bookingId = response.path("id").asText();
                final String serviceName = response.path("serviceName").asText();
                final String branchName = response.path("staffNames").asText();
                final String dateTime = response.path("startDateTime").asText();
                final String status = response.path("isConfirmed").asBoolean() ? "confirmed" : "pending";
                return new BookingConfirmation() {
                    @Override
                    public String getBookingId() { return bookingId; }
                    @Override
                    public String getServiceName() { return serviceName; }
                    @Override
                    public String getBranchName() { return branchName; }
                    @Override
                    public String getDateTime() { return dateTime; }
                    @Override
                    public String getStatus() { return status; }
                };
            }
        } catch (Exception e) {
            log.error("Error booking appointment", e);
        }
        return null;
    }

    @Override
    public boolean confirmBooking(String bookingId) {
        try {
            String url = MS_GRAPH_API + "/solutions/bookingBusinesses/" + BOOKING_BUSINESS_ID
                    + "/appointments/" + bookingId + "/confirm";
            JsonNode response = callMSGraphPost(url, mapper.createObjectNode());
            return response != null;
        } catch (Exception e) {
            log.error("Error confirming booking", e);
            return false;
        }
    }

    @Override
    public boolean cancelBooking(String bookingId) {
        try {
            String url = MS_GRAPH_API + "/solutions/bookingBusinesses/" + BOOKING_BUSINESS_ID
                    + "/appointments/" + bookingId + "/cancel";
            JsonNode response = callMSGraphPost(url, mapper.createObjectNode());
            return response != null;
        } catch (Exception e) {
            log.error("Error cancelling booking", e);
            return false;
        }
    }

    private JsonNode callMSGraph(String url) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        try {
            HttpGet get = new HttpGet(url);
            get.setHeader("Authorization", "Bearer " + accessToken);
            get.setHeader("Content-Type", "application/json");
            try (CloseableHttpResponse response = client.execute(get)) {
                String json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                return mapper.readTree(json);
            }
        } finally {
            client.close();
        }
    }

    private JsonNode callMSGraphPost(String url, ObjectNode body) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        try {
            HttpPost post = new HttpPost(url);
            post.setHeader("Authorization", "Bearer " + accessToken);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(mapper.writeValueAsString(body), StandardCharsets.UTF_8));
            try (CloseableHttpResponse response = client.execute(post)) {
                String json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                return mapper.readTree(json);
            }
        } finally {
            client.close();
        }
    }
}
