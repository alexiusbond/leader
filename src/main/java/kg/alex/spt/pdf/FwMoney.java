package kg.alex.spt.pdf;

interface WritableSumm {
    String numberToString(Number num);
}

// нечто абстрактное
abstract class AbstractWritableSumm implements WritableSumm {
    // получение единиц, 11-19, десятков, сотен
    abstract protected String getS1(int n, int gender);
    abstract protected String getS11(int n);
    abstract protected String getS10(int n);
    abstract protected String getS100(int n);
    // преобразование триады в слова
    protected String triadToString(int n, int gender, boolean acceptZero) {
        if (!acceptZero && n == 0) return "";
        String res = "";
        if (n % 1000 > 99) {
            res += getS100(n % 1000 / 100) + " ";
        }
        if (n % 100 > 10 && n % 100 < 20) {
            return res + getS11(n % 10) + " ";
        }
        if (n % 100 > 9) {
            res += getS10(n % 100 / 10) + " ";
        }
        if (res.length() == 0 || n % 10 > 0) {
            res = res + getS1(n % 10, gender) + " ";
        }
        return res;
    }
    // получение юнита (название триады или валюта)
    abstract protected String getUnit(int idx, long count);
    // форма юнита (для русского языка - пол)
    abstract protected int getUnitGender(int idx);
    // наш главный метод
    public String numberToString(Number num) {
        String res = "";
        if (num.longValue() == 0) {
            res = getS1(0, 0) + " " + getUnit(1, 0);
        }
        int idx = 0;
        num = num.longValue() * 1000 + (long)((num.doubleValue() - num.longValue()) * 100);
        while (num.longValue() > 0) {
            String triad = triadToString((int)(num.longValue() % 1000),
                    getUnitGender(idx), idx < 1);
            res = triad + getUnit(idx, num.longValue() % 1000) + " " + res;
            num = num.longValue() / 1000;
            idx++;
        }
        return res;
    }
}

abstract class WritableSummRu extends AbstractWritableSumm {
    final String[][] str1 = {
            {"ноль","один","два","три","четыре","пять","шесть","семь","восемь","девять"},
            {"ноль","одна","две","три","четыре","пять","шесть","семь","восемь","девять"},
    };
    final String[] str100 = {"", "сто", "двести", "триста", "четыреста", "пятьсот",
            "шестьсот", "семьсот", "восемьсот", "девятьсот"};
    String[] str11 = {"", "одиннадцать", "двенадцать", "тринадцать", "четырнадцать",
            "пятнадцать", "шестнадцать", "семнадцать", "восемнадцать", "девятнадцать",
            "двадцать"};
    String[] str10 = {"", "десять", "двадцать", "тридцать", "сорок", "пятьдесят",
            "шестьдесят", "семьдесят", "восемьдесят", "девяносто"};
    final String[][] forms = {
            {"", "", "", "0"},
            {"", "", "", "0"},
            {"тысяча", "тысячи", "тысяч", "1"},
            {"миллион", "миллиона", "миллионов", "0"},
            {"миллиард", "миллиарда", "миллиардов", "0"},
            {"триллион", "триллиона", "триллионов", "0"},
    };
    protected String getS1(int n, int gender) {
        return str1[gender][n];
    }
    protected String getS11(int n) {
        return str11[n];
    }
    protected String getS10(int n) {
        return str10[n];
    }
    protected String getS100(int n) {
        return str100[n];
    }
    protected int getUnitGender(int idx) {
        return new Integer(forms[idx][3]);
    }
    protected String getUnit(int idx, long cnt) {
        if (cnt % 100 > 4 && cnt % 100 < 21)
            return forms[idx][2];
        switch ((int)(cnt % 10)) {
            case 1:
                return forms[idx][0];
            case 2:
            case 3:
            case 4:
                return forms[idx][1];
            default:
                return forms[idx][2];
        }
    }
}


class WritableSummRuUSD extends WritableSummRu {
    {
        forms[0] = new String[]{"цент", "цента", "центов", "0"};
        forms[1] = new String[]{"доллар", "доллара", "долларов", "0"};
    }
}
class WritableSummRuSOM extends WritableSummRu {
    {
        forms[0] = new String[]{"тыйын", "тыйына", "тыйынов", "1"};
        forms[1] = new String[]{"сом", "сома", "сомов", "0"};
    }
}
