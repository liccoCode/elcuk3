package mocks;

import models.User;
import org.junit.Test;
import play.test.UnitTest;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 12/14/12
 * Time: 11:53 AM
 */
public class MockTest extends UnitTest {

    List<String> mockList = mock(List.class);

    User u = mock(User.class);

    @Test
    public void testMock() {
        when(mockList.get(0)).thenReturn("one");

        System.out.println(mockList.get(0));

        verify(mockList).get(0);
    }

    @Test
    public void testUserMock() {
        when(u.authenticate("kdjf")).thenReturn(true);

        assertEquals(true, u.authenticate("kdjf"));
    }
}
