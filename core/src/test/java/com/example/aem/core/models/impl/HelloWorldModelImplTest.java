package com.example.aem.core.models.impl;

import com.example.aem.core.models.HelloWorldModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class HelloWorldModelImplTest {

    @Test
    public void testDefaultName() {
        HelloWorldModelImpl model = new HelloWorldModelImpl();
        assertEquals("World", model.getName());
    }

    @Test
    public void testCustomName() {
        HelloWorldModelImpl model = new HelloWorldModelImpl();
        model.setName("Test");
        assertEquals("Test", model.getName());
    }

    @Test
    public void testMessage() {
        HelloWorldModelImpl model = new HelloWorldModelImpl();
        model.init();
        assertNotNull(model.getMessage());
        assertTrue(model.getMessage().startsWith("Hello"));
    }
}