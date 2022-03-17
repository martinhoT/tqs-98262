package tqs.lab2;

import java.util.ArrayList;
import java.util.List;

public class StocksPortfolio {

    private List<Stock> stocks;
    private IStockMarketService stockMarket;

    public StocksPortfolio(IStockMarketService stockMarket) {
        stocks = new ArrayList<>();
        this.stockMarket = stockMarket;
    }

    public void addStock(Stock stock) {
        stocks.add(stock);
    }

    public double getTotalValue() {
        return stocks.stream()
            .map((stock) -> stockMarket.lookUpPrice(stock.getLabel()) * stock.getQuantity())
            .reduce(0d, Double::sum);
    }

}
