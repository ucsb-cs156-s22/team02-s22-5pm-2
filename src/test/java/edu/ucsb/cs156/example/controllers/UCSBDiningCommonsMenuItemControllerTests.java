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

        // @Test
        // public void logged_out_users_cannot_get_by_id() throws Exception {
        //         mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem?id=1"))
        //                         .andExpect(status().is(403)); // logged out users can't get by id
        // }

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

        // @WithMockUser(roles = { "USER" })
        // @Test
        // public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {
        //
        //         // arrange
        //
        //         UCSBDiningCommonsMenuItem commons = UCSBDiningCommonsMenuItem.builder()
        //                         .name("Carrillo")
        //                         .code("carrillo")
        //                         .hasSackMeal(false)
        //                         .hasTakeOutMeal(false)
        //                         .hasDiningCam(true)
        //                         .latitude(34.409953)
        //                         .longitude(-119.85277)
        //                         .build();
        //
        //         when(ucsbDiningCommonsMenuItemRepository.findById(eq("carrillo"))).thenReturn(Optional.of(commons));
        //
        //         // act
        //         MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem?code=carrillo"))
        //                         .andExpect(status().isOk()).andReturn();
        //
        //         // assert
        //
        //         verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq("carrillo"));
        //         String expectedJson = mapper.writeValueAsString(commons);
        //         String responseString = response.getResponse().getContentAsString();
        //         assertEquals(expectedJson, responseString);
        // }
        //
        // @WithMockUser(roles = { "USER" })
        // @Test
        // public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {
        //
        //         // arrange
        //
        //         when(ucsbDiningCommonsMenuItemRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());
        //
        //         // act
        //         MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem?code=munger-hall"))
        //                         .andExpect(status().isNotFound()).andReturn();
        //
        //         // assert
        //
        //         verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq("munger-hall"));
        //         Map<String, Object> json = responseToJson(response);
        //         assertEquals("EntityNotFoundException", json.get("type"));
        //         assertEquals("UCSBDiningCommonsMenuItem with id munger-hall not found", json.get("message"));
        // }

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
        public void an_admin_user_can_post_a_new_commons() throws Exception {
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

        // @WithMockUser(roles = { "ADMIN", "USER" })
        // @Test
        // public void admin_can_delete_a_date() throws Exception {
        //         // arrange
        //
        //         UCSBDiningCommonsMenuItem portola = UCSBDiningCommonsMenuItem.builder()
        //                         .name("Portola")
        //                         .code("portola")
        //                         .hasSackMeal(true)
        //                         .hasTakeOutMeal(true)
        //                         .hasDiningCam(true)
        //                         .latitude(34.417723)
        //                         .longitude(-119.867427)
        //                         .build();
        //
        //         when(ucsbDiningCommonsMenuItemRepository.findById(eq("portola"))).thenReturn(Optional.of(portola));
        //
        //         // act
        //         MvcResult response = mockMvc.perform(
        //                         delete("/api/ucsbdiningcommonsmenuitem?code=portola")
        //                                         .with(csrf()))
        //                         .andExpect(status().isOk()).andReturn();
        //
        //         // assert
        //         verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById("portola");
        //         verify(ucsbDiningCommonsMenuItemRepository, times(1)).delete(any());
        //
        //         Map<String, Object> json = responseToJson(response);
        //         assertEquals("UCSBDiningCommonsMenuItem with id portola deleted", json.get("message"));
        // }
        //
        // @WithMockUser(roles = { "ADMIN", "USER" })
        // @Test
        // public void admin_tries_to_delete_non_existant_commons_and_gets_right_error_message()
        //                 throws Exception {
        //         // arrange
        //
        //         when(ucsbDiningCommonsMenuItemRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());
        //
        //         // act
        //         MvcResult response = mockMvc.perform(
        //                         delete("/api/ucsbdiningcommonsmenuitem?code=munger-hall")
        //                                         .with(csrf()))
        //                         .andExpect(status().isNotFound()).andReturn();
        //
        //         // assert
        //         verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById("munger-hall");
        //         Map<String, Object> json = responseToJson(response);
        //         assertEquals("UCSBDiningCommonsMenuItem with id munger-hall not found", json.get("message"));
        // }
        //
        // @WithMockUser(roles = { "ADMIN", "USER" })
        // @Test
        // public void admin_can_edit_an_existing_commons() throws Exception {
        //         // arrange
        //
        //         UCSBDiningCommonsMenuItem carrilloOrig = UCSBDiningCommonsMenuItem.builder()
        //                         .name("Carrillo")
        //                         .code("carrillo")
        //                         .hasSackMeal(false)
        //                         .hasTakeOutMeal(false)
        //                         .hasDiningCam(true)
        //                         .latitude(34.409953)
        //                         .longitude(-119.85277)
        //                         .build();
        //
        //         UCSBDiningCommonsMenuItem carrilloEdited = UCSBDiningCommonsMenuItem.builder()
        //                         .name("Carrillo Dining Hall")
        //                         .code("carrillo")
        //                         .hasSackMeal(true)
        //                         .hasTakeOutMeal(true)
        //                         .hasDiningCam(false)
        //                         .latitude(34.409954)
        //                         .longitude(-119.85278)
        //                         .build();
        //
        //         String requestBody = mapper.writeValueAsString(carrilloEdited);
        //
        //         when(ucsbDiningCommonsMenuItemRepository.findById(eq("carrillo"))).thenReturn(Optional.of(carrilloOrig));
        //
        //         // act
        //         MvcResult response = mockMvc.perform(
        //                         put("/api/ucsbdiningcommonsmenuitem?code=carrillo")
        //                                         .contentType(MediaType.APPLICATION_JSON)
        //                                         .characterEncoding("utf-8")
        //                                         .content(requestBody)
        //                                         .with(csrf()))
        //                         .andExpect(status().isOk()).andReturn();
        //
        //         // assert
        //         verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById("carrillo");
        //         verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(carrilloEdited); // should be saved with updated info
        //         String responseString = response.getResponse().getContentAsString();
        //         assertEquals(requestBody, responseString);
        // }
        //
        // @WithMockUser(roles = { "ADMIN", "USER" })
        // @Test
        // public void admin_cannot_edit_commons_that_does_not_exist() throws Exception {
        //         // arrange
        //
        //         UCSBDiningCommonsMenuItem editedCommons = UCSBDiningCommonsMenuItem.builder()
        //                         .name("Munger Hall")
        //                         .code("munger-hall")
        //                         .hasSackMeal(false)
        //                         .hasTakeOutMeal(false)
        //                         .hasDiningCam(true)
        //                         .latitude(34.420799)
        //                         .longitude(-119.852617)
        //                         .build();
        //
        //         String requestBody = mapper.writeValueAsString(editedCommons);
        //
        //         when(ucsbDiningCommonsMenuItemRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());
        //
        //         // act
        //         MvcResult response = mockMvc.perform(
        //                         put("/api/ucsbdiningcommonsmenuitem?code=munger-hall")
        //                                         .contentType(MediaType.APPLICATION_JSON)
        //                                         .characterEncoding("utf-8")
        //                                         .content(requestBody)
        //                                         .with(csrf()))
        //                         .andExpect(status().isNotFound()).andReturn();
        //
        //         // assert
        //         verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById("munger-hall");
        //         Map<String, Object> json = responseToJson(response);
        //         assertEquals("UCSBDiningCommonsMenuItem with id munger-hall not found", json.get("message"));
        //
        // }
}
