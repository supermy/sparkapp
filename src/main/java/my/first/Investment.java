package my.first;

/**
 * Created by moyong on 16/6/12.
 */
public class Investment {
    private String investmentName;
    private InvestmentType investmentType;

    public Investment(String name, InvestmentType type) {
        investmentName = name;
        investmentType = type;
    }

    public int yield() {
        return 0;
    }
}
