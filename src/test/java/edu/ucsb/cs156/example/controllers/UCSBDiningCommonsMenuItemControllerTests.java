package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuItemControllerTests extends ControllerTestCase {

        @MockBean
        UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/ucsbdiningcommonsmenuitem/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem?id=1"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/ucsbdiningcommonsmenuitem/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsbdiningcommonsmenuitem/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsbdiningcommonsmenuitem/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                UCSBDiningCommonsMenuItem salad = UCSBDiningCommonsMenuItem.builder()
                                .name("Chicken Caesar Salad")
                                .diningCommonsCode("ortega")
                                .station("Entrees")
                                .build();

                when(ucsbDiningCommonsMenuItemRepository.findById(eq(1L))).thenReturn(Optional.of(salad));

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem?id=1"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq(1L));
                String expectedJson = mapper.writeValueAsString(salad);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(ucsbDiningCommonsMenuItemRepository.findById(eq(2L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem?id=2"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq(2L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBDiningCommonsMenuItem with id 2 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsbdiningcommonsmenuitem() throws Exception {

                // arrange

                UCSBDiningCommonsMenuItem salad = UCSBDiningCommonsMenuItem.builder()
                                .name("Chicken Caesar Salad")
                                .diningCommonsCode("ortega")
                                .station("Entrees")
                                .build();

                UCSBDiningCommonsMenuItem soup = UCSBDiningCommonsMenuItem.builder()
                                .name("Cream of Broccoli Soup (v)")
                                .diningCommonsCode("portola")
                                .station("Greens & Grains")
                                .build();

                ArrayList<UCSBDiningCommonsMenuItem> expectedMenuItem = new ArrayList<>();
                expectedMenuItem.addAll(Arrays.asList(salad, soup));

                when(ucsbDiningCommonsMenuItemRepository.findAll()).thenReturn(expectedMenuItem);

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbDiningCommonsMenuItemRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedMenuItem);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_menu_item() throws Exception {
                // arrange

                UCSBDiningCommonsMenuItem tofu = UCSBDiningCommonsMenuItem.builder()
                                .name("Tofu Banh Mi Sandwich (v)")
                                .diningCommonsCode("ortega")
                                .station("Entree Specials")
                                .build();

                when(ucsbDiningCommonsMenuItemRepository.save(eq(tofu))).thenReturn(tofu);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/ucsbdiningcommonsmenuitem/post?name=Tofu Banh Mi Sandwich (v)&diningCommonsCode=ortega&station=Entree Specials")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(tofu);
                String expectedJson = mapper.writeValueAsString(tofu);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_menu_item() throws Exception {
                // arrange

                UCSBDiningCommonsMenuItem salad = UCSBDiningCommonsMenuItem.builder()
                                .name("Chicken Caesar Salad")
                                .diningCommonsCode("ortega")
                                .station("Entrees")
                                .build();

                when(ucsbDiningCommonsMenuItemRepository.findById(eq(1L))).thenReturn(Optional.of(salad));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsbdiningcommonsmenuitem?id=1")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(1L);
                verify(ucsbDiningCommonsMenuItemRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDiningCommonsMenuItem with id 1 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_menu_item_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(ucsbDiningCommonsMenuItemRepository.findById(eq(1L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsbdiningcommonsmenuitem?id=1")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(1L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDiningCommonsMenuItem with id 1 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_menu_item() throws Exception {
                // arrange

                UCSBDiningCommonsMenuItem saladOrig = UCSBDiningCommonsMenuItem.builder()
                                .name("Chicken Caesar Salad")
                                .diningCommonsCode("ortega")
                                .station("Entrees")
                                .build();

                UCSBDiningCommonsMenuItem saladEdited = UCSBDiningCommonsMenuItem.builder()
                                .name("Hot Dog Caesar Salad")
                                .diningCommonsCode("dlg")
                                .station("Salad")
                                .build();

                String requestBody = mapper.writeValueAsString(saladEdited);

                when(ucsbDiningCommonsMenuItemRepository.findById(eq(1L))).thenReturn(Optional.of(saladOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsbdiningcommonsmenuitem?id=1")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(1L);
                verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(saladEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_menu_item_that_does_not_exist() throws Exception {
                // arrange

                UCSBDiningCommonsMenuItem saladEdited = UCSBDiningCommonsMenuItem.builder()
                                .name("Chicken Caesar Salad")
                                .diningCommonsCode("dlg")
                                .station("Entrees")
                                .build();

                String requestBody = mapper.writeValueAsString(saladEdited);

                when(ucsbDiningCommonsMenuItemRepository.findById(eq(1L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsbdiningcommonsmenuitem?id=1")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(1L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDiningCommonsMenuItem with id 1 not found", json.get("message"));

        }
}
