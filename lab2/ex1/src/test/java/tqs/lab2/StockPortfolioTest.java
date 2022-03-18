package tqs.lab2;

//import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StockPortfolioTest {

    @Mock
    private IStockMarketService stockMarket;

    private StocksPortfolio stocksPortfolio;

    @BeforeEach
    public void beforeEach() {
        stocksPortfolio = new StocksPortfolio(stockMarket);
    }
    
    @AfterEach
    public void afterEach() {}
    
    
    
    @Test
    public void testGetTotalValue() {
        when(stockMarket.lookUpPrice("GME")).thenReturn(9d);
        when(stockMarket.lookUpPrice("WUT")).thenReturn(5d);
        when(stockMarket.lookUpPrice("ABC")).thenReturn(10d);

        stocksPortfolio.addStock(new Stock("GME", 100));
        stocksPortfolio.addStock(new Stock("WUT", 999));
        stocksPortfolio.addStock(new Stock("ABC", 123));

        //assertEquals(stocksPortfolio.getTotalValue(), 9*100 + 5*999 + 10*123);
        assertThat(stocksPortfolio.getTotalValue(), is(9d*100 + 5d*999 + 10d*123));

        verify(stockMarket, times(1)).lookUpPrice("GME");
        verify(stockMarket, times(1)).lookUpPrice("WUT");
        verify(stockMarket, times(1)).lookUpPrice("ABC");
    }

    
}
