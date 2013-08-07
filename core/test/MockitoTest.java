import factory.FactoryBoy;
import models.market.Orderr;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import play.test.UnitTest;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 5/29/13
 * Time: 11:45 AM
 */
public class MockitoTest extends UnitTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    /**
     * Mock 测试用于验证对象方法的行为
     */
    public void testVerify() {
        // 将对象进行 Mock
        List<String> mockedList = mock(List.class);

        // 对 mock 对象进行操作
        mockedList.add("one");
        mockedList.clear();

        // 验证 mock 方法调用
        verify(mockedList).add("one");
        verify(mockedList).clear();
    }

    @Test
    /**
     * Stub 测试用于替换掉测试对象的方法
     */
    public void testStubbing() {
        LinkedList<String> mockedList = mock(LinkedList.class);

        // 下面是对 mock 进行 stub, 两种方式, 结果一样
        stub(mockedList.get(0)).toReturn("first");
        when(mockedList.get(1)).thenThrow(new RuntimeException());

        // 对 stub 的状态测试
        assertEquals("first", mockedList.get(0));
        assertThat(mockedList.get(0), is("first"));
        assertEquals(null, mockedList.get(999));

        // 对 mock 的行为进行测试
        verify(mockedList).get(0);

        // 对 stub 的异常测试
        thrown.expect(RuntimeException.class);
        mockedList.get(1);
    }

    @Test
    public void testArgumentMatchers() {
        List<String> mockedList = mock(List.class);

        when(mockedList.get(anyInt())).thenReturn("element");

        when(mockedList.contains(anyString())).thenReturn(true);

        assertThat(mockedList.get(999), is("element"));
        assertThat(mockedList.get(9), is("element"));

        verify(mockedList, times(2)).get(anyInt());
    }

    @Test
    public void testFactory() {
        Orderr order = FactoryBoy.build(Orderr.class);
        assertThat(order, is(notNullValue()));
        assertThat(order.state, is(Orderr.S.SHIPPED));
    }

}
