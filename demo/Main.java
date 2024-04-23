import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
// 引用 json 解析包
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

public class Main {

    /**
     * 创建钱包方法
     *
     * 具体 API 说明 请参考：https://blockwallets.io/docs/api/wallet-create
     *
     * @param apiKey 用户后台查看的 api_key
     * @return 新创建钱包相关信息
     */
    public static JsonObject createWallet(String apiKey) throws IOException {
        // 构建请求
        URL url = new URL("https://api.blockwallets.io/api/v1/block/wallets");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("api-key", apiKey);
        connection.setDoOutput(true);

        // 请求参数
        String jsonInputString = "{\"block_category_id\": 1}";

        // 发送请求
        connection.getOutputStream().write(jsonInputString.getBytes());
        if (connection.getResponseCode() != 200) {
            System.out.println("请求创建钱包失败！");

            return null;
        }
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        
        // 解析 JSON 数据
        String responseBodyString = response.toString();
        JsonObject jsonObject = JsonParser.parseString(responseBodyString).getAsJsonObject();
        // 获取 data 字段的值
        JsonObject dataObject = jsonObject.getAsJsonObject("data");
        // 获取 result 字段的值
        JsonObject resultObject = dataObject.getAsJsonObject("result");

        System.out.println("请求创建钱包响应数据: " + jsonObject);

        return resultObject;
    }

    /**
     * 查询钱包最新交易列表信息
     *
     * 具体 API 说明 请参考：https://blockwallets.io/docs/api/transaction-query
     *
     * @param apiKey 用户后台查看的 api_key
     * @param address 钱包地址
     * @param tokenType 代币类型（TRX、TRC20）[默认：TRC20 对应的是 USDT]
     * @return 交易列表信息
     */
    public static JsonArray transactionQuery(String apiKey, String address, String tokenType) throws IOException {
        URL url = new URL("https://api.blockwallets.io/api/v1/block/trons/transaction/record/gather?address=" + address + "&token_type=" + tokenType);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("accept", "application/json");
        connection.setRequestProperty("api-key", apiKey);

        if (connection.getResponseCode() != 200) {
            System.out.println("查询钱包最新交易列表信息失败！");

            return null;
        }
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // 解析 JSON 数据
        String responseBodyString = response.toString();
        JsonObject jsonObject = JsonParser.parseString(responseBodyString).getAsJsonObject();
        // 获取 data 字段的值
        JsonObject dataObject = jsonObject.getAsJsonObject("data");
        // 获取 result 字段的值
        JsonArray resultsArray = dataObject.getAsJsonArray("results");

        System.out.println("请求查询钱包最新交易列表信息响应数据：" + jsonObject);

        return resultsArray;
    }

    public static void main(String[] args) {
        String apiKey = "替换成用户后台查看的 Api Key";

        try {
            // 1. 创建钱包
            JsonObject resultObject = createWallet(apiKey);

            if (resultObject != null) {
                // 提取钱包地址
                String address = resultObject.get("address").getAsString();
                System.out.println(address);

                // 2. 等待用户支付
                System.out.println("新创建的钱包地址为：" + address);
                String transactionType = "TRX";
                System.out.println("请往地址为 " + address + " 的钱包里，支付一定的 " + transactionType + " 金额(最低：0.000001 " + transactionType + ")");

                // 3. 轮询的方式查询最新交易记录
                System.out.println("正在查询钱包 " + address + " 最新交易记录，请稍候...");
                String tokenType = transactionType.equals("TRX") ? "TRX" : "TRC20";

                while (true) {
                    JsonArray resultsArray = transactionQuery(apiKey, address, tokenType);

                    // 判断是否有数据返回
                    if (resultsArray != null && resultsArray.size() > 0) {
                        // 访问数组中最新一条数据
                        JsonObject firstResult = resultsArray.get(0).getAsJsonObject();
                        Double amount = firstResult.get("amount").getAsDouble();

                        System.out.println("本次最新交易金额为 " + String.format("%.6f", amount) + " " + transactionType);

                        break;
                    }

                    // 延迟
                    Thread.sleep(20000);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
