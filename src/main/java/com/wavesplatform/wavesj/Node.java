package com.wavesplatform.wavesj;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wavesplatform.wavesj.json.WavesJsonMapper;
import com.wavesplatform.wavesj.matcher.CancelOrder;
import com.wavesplatform.wavesj.matcher.DeleteOrder;
import com.wavesplatform.wavesj.matcher.Order;
import com.wavesplatform.wavesj.transactions.LeaseTransaction;
import com.wavesplatform.wavesj.transactions.TransferTransaction;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Node {
    private static final String DEFAULT_NODE = "http://pool.testnet.wavesnodes.com";

    private static final TypeReference<OrderBook> ORDER_BOOK = new TypeReference<OrderBook>() {};
    private static final TypeReference<List<Order>> ORDER_LIST = new TypeReference<List<Order>>() {};
    private static final TypeReference<OrderStatusInfo> ORDER_STATUS = new TypeReference<OrderStatusInfo>() {};
    private static final TypeReference<Map<String, Object>> TX_INFO = new TypeReference<Map<String, Object>>() {};

    private final URI uri;
    private final WavesJsonMapper wavesJsonMapper;
    private final CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(
            RequestConfig.custom()
                    .setSocketTimeout(5000)
                    .setConnectTimeout(5000)
                    .setConnectionRequestTimeout(5000)
                    .setCookieSpec(CookieSpecs.STANDARD)
                    .build())
            .build();

    public Node() {
        try {
            this.uri = new URI(DEFAULT_NODE);
            this.wavesJsonMapper = new WavesJsonMapper((byte) 'T');
        } catch (URISyntaxException e) {
            // should not happen
            throw new RuntimeException(e);
        }
    }

    public Node(String uri, char chainId) throws URISyntaxException {
        this.uri = new URI(uri);
        this.wavesJsonMapper = new WavesJsonMapper((byte) chainId);
    }

    public String getVersion() throws IOException {
        return send("/node/version", "version").asText();
    }

    public int getHeight() throws IOException {
        return send("/blocks/height", "height").asInt();
    }

    public long getBalance(String address) throws IOException {
        return send("/addresses/balance/" + address, "balance").asLong();
    }

    public long getBalance(String address, int confirmations) throws IOException {
        return send("/addresses/balance/" + address + "/" + confirmations, "balance").asLong();
    }

    public long getBalance(String address, String assetId) throws IOException {
        return Asset.isWaves(assetId)
                ? getBalance(address)
                : send("/assets/balance/" + address + "/" + assetId, "balance").asLong();
    }

    /**
     * Returns object by its ID.
     *
     * @param txId object ID
     * @return object object
     * @throws IOException if no object with the given ID exists
     */
    public Transaction getTransaction(String txId) throws IOException {
        return wavesJsonMapper.convertValue(send("/transactions/info/" + txId), Transaction.class);
    }

    public Map<String, Object> getTransactionData(String txId) throws IOException {
        return wavesJsonMapper.convertValue(send("/transactions/info/" + txId), TX_INFO);
    }

    /**
     * Returns block at given height.
     *
     * @param height blockchain height
     * @return block object
     * @throws IOException if no block exists at the given height
     */
    public Block getBlock(int height) throws IOException {
        return wavesJsonMapper.convertValue(send("/blocks/at/" + height), Block.class);
    }

    /**
     * Returns block by its signature.
     *
     * @param signature block signature
     * @return block object
     * @throws IOException if no block with the given signature exists
     */
    public Block getBlock(String signature) throws IOException {
        return wavesJsonMapper.convertValue(send("/blocks/signature/" + signature), Block.class);
    }

    public boolean validateAddresses(String address) throws IOException {
        return send("/addresses/validate/" + address, "valid").asBoolean();
    }

    public String getAddrByAlias(String alias) throws IOException {
        return send("/alias/by-alias/" + alias, "address").textValue();
    }

    /**
     * Sends a signed object and returns its ID.
     *
     * @param tx signed object (as created by static methods in Transaction class)
     * @return Transaction ID
     * @throws IOException
     */
    public String send(ApiJson tx) throws IOException {
        return parse(exec(request(tx)), "id").asText();
    }

    private JsonNode send(String path, String... key) throws IOException {
        return parse(exec(request(path)), key);
    }

    public String transfer(PrivateKeyAccount from, String recipient, long amount, long fee, String message) throws IOException {
        ObjectWithProofs<TransferTransaction> tx = Transaction.makeTransferTx(from, recipient, amount, null, fee, null, message);
        return send(tx);
    }

    public String transfer(PrivateKeyAccount from, String assetId, String recipient,
                           long amount, long fee, String feeAssetId, String message) throws IOException {
        ObjectWithProofs<TransferTransaction> tx = Transaction.makeTransferTx(from, recipient, amount, assetId, fee, feeAssetId, message);
        return send(tx);
    }

    public String lease(PrivateKeyAccount from, String recipient, long amount, long fee) throws IOException {
        ObjectWithProofs<LeaseTransaction> tx = Transaction.makeLeaseTx(from, recipient, amount, fee);
        return send(tx);
    }

    public String cancelLease(PrivateKeyAccount account, byte chainId, String txId, long fee) throws IOException {
        return send(Transaction.makeLeaseCancelTx(account, chainId, txId, fee));
    }

    public String issueAsset(PrivateKeyAccount account, byte chainId, String name, String description, long quantity,
                             byte decimals, boolean reissuable, String script, long fee) throws IOException {
        return send(Transaction.makeIssueTx(account, chainId, name, description, quantity, decimals, reissuable, script, fee));
    }

    public String reissueAsset(PrivateKeyAccount account, byte chainId, String assetId, long quantity, boolean reissuable, long fee) throws IOException {
        return send(Transaction.makeReissueTx(account, chainId, assetId, quantity, reissuable, fee));
    }

    public String burnAsset(PrivateKeyAccount account, byte chainId, String assetId, long amount, long fee) throws IOException {
        return send(Transaction.makeBurnTx(account, chainId, assetId, amount, fee));
    }

    public String sponsorAsset(PrivateKeyAccount account, String assetId, long minAssetFee, long fee) throws IOException {
        return send(Transaction.makeSponsorTx(account, assetId, minAssetFee, fee));
    }

    public String alias(PrivateKeyAccount account, byte chainId, String alias, long fee) throws IOException {
        return send(Transaction.makeAliasTx(account, alias, chainId, fee));
    }

    public String massTransfer(PrivateKeyAccount from, String assetId, Collection<Transfer> transfers, long fee, String message) throws IOException {
        return send(Transaction.makeMassTransferTx(from, assetId, transfers, fee, message));
    }

    public String data(PrivateKeyAccount from, Collection<DataEntry<?>> data, long fee) throws IOException {
        return send(Transaction.makeDataTx(from, data, fee));
    }

    /**
     * Sets a validating script for an account.
     *
     * @param from    the account
     * @param script  script text
     * @param chainId chain ID
     * @param fee     object fee
     * @return object ID
     * @throws IOException if an error occurs
     * @see Account#MAINNET
     * @see Account#TESTNET
     */
    public String setScript(PrivateKeyAccount from, String script, byte chainId, long fee) throws IOException {
        return send(Transaction.makeScriptTx(from, compileScript(script), chainId, fee));
    }

    /**
     * Compiles a script.
     *
     * @param script the script to compile
     * @return compiled script, base64 encoded
     * @throws IOException if the script is not well formed or some other error occurs
     */
    public String compileScript(String script) throws IOException {
        if (script == null || script.isEmpty()) {
            return null;
        }
        HttpPost request = new HttpPost(uri.resolve("/utils/script/compile"));
        request.setEntity(new StringEntity(script));
        return parse(exec(request), "script").asText();
    }

    // Matcher transactions

    public String getMatcherKey() throws IOException {
        return parse(exec(request("/matcher"))).asText();
    }

    public Order createOrder(PrivateKeyAccount account, String matcherKey, AssetPair assetPair, Order.Type orderType,
                             long price, long amount, long expiration, long matcherFee) throws IOException {
        ObjectWithSignature<Order> tx = Transaction.makeOrderTx(account, matcherKey, orderType, assetPair, price, amount, expiration, matcherFee);
        JsonNode tree = parse(exec(request(tx)));
        // fix order status
        ObjectNode message = (ObjectNode) tree.get("message");
        message.put("status", tree.get("status").asText());
        return wavesJsonMapper.treeToValue(tree.get("message"), Order.class);
    }

    public String cancelOrder(PrivateKeyAccount account, AssetPair assetPair, String orderId) throws IOException {
        ApiJson tx = Transaction.makeOrderCancelTx(account, assetPair, orderId);
        return parse(exec(request(tx)), "status").asText();
    }

    public String deleteOrder(PrivateKeyAccount account, AssetPair assetPair, String orderId) throws IOException {
        ApiJson tx = Transaction.makeDeleteOrder(account, assetPair, orderId);
        return parse(exec(request(tx)), "status").asText();
    }

    public OrderBook getOrderBook(AssetPair assetPair) throws IOException {
        String path = "/matcher/orderbook/" + assetPair.amountAsset + '/' + assetPair.priceAsset;
        return parse(exec(request(path)), ORDER_BOOK);
    }

    public OrderStatusInfo getOrderStatus(String orderId, AssetPair assetPair) throws IOException {
        String path = "/matcher/orderbook/" + assetPair.amountAsset + '/' + assetPair.priceAsset + '/' + orderId;
        return parse(exec(request(path)), ORDER_STATUS);
    }

    public List<Order> getOrders(PrivateKeyAccount account) throws IOException {
        return getOrders(account, "/matcher/orderbook/" + Base58.encode(account.getPublicKey()));
    }

    public List<Order> getOrders(PrivateKeyAccount account, AssetPair market) throws IOException {
        return getOrders(account, String.format("/matcher/orderbook/%s/%s/publicKey/%s",
                market.amountAsset, market.priceAsset, Base58.encode(account.getPublicKey())));
    }

    private List<Order> getOrders(PrivateKeyAccount account, String path) throws IOException {
        long timestamp = System.currentTimeMillis();
        ByteBuffer buf = ByteBuffer.allocate(40);
        buf.put(account.getPublicKey()).putLong(timestamp);
        String signature = account.sign(buf.array());
        HttpResponse r = exec(request(path, "Timestamp", String.valueOf(timestamp), "Signature", signature));
        return parse(r, ORDER_LIST);
    }

    private <T> HttpUriRequest request(String path, String... headers) {
        HttpUriRequest req = new HttpGet(uri.resolve(path));
        for (int i = 0; i < headers.length; i += 2) {
            req.addHeader(headers[i], headers[i + 1]);
        }
        return req;
    }

    private HttpUriRequest request(ApiJson obj) throws JsonProcessingException {
        String endpoint;
        if (obj instanceof ProofedObject) {
            Object o = ((ProofedObject) obj).getObject();
            if (o instanceof Transaction) {
                endpoint = "/transactions/broadcast";
            } else if (o instanceof Order) {
                endpoint = "/matcher/orderbook";
            } else if (o instanceof CancelOrder) {
                CancelOrder co = (CancelOrder) o;
                endpoint = "/matcher/orderbook/" + co.getAssetPair().amountAsset + '/' + co.getAssetPair().priceAsset + "/cancel";
            } else if (o instanceof DeleteOrder) {
                DeleteOrder d = (DeleteOrder) o;
                endpoint = "/matcher/orderbook/" + d.getAssetPair().amountAsset + '/' + d.getAssetPair().priceAsset + "/delete";
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            throw new IllegalArgumentException();
        }
        HttpPost request = new HttpPost(uri.resolve(endpoint));
        request.setEntity(new StringEntity(wavesJsonMapper.writeValueAsString(obj), ContentType.APPLICATION_JSON));
        return request;
    }

    private HttpResponse exec(HttpUriRequest request) throws IOException {
        HttpResponse r = client.execute(request);
        if (r.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            try {
                throw new IOException(EntityUtils.toString(r.getEntity()));
            } catch (JsonParseException e) {
                throw new RuntimeException("Server error " + r.getStatusLine().getStatusCode());
            }
        }
        return r;
    }

    private <T> T parse(HttpResponse r, TypeReference<T> ref) throws IOException {
        return wavesJsonMapper.readValue(r.getEntity().getContent(), ref);
    }

    private JsonNode parse(HttpResponse r, String... keys) throws IOException {
        JsonNode tree = wavesJsonMapper.readTree(r.getEntity().getContent());
        for (String key : keys) {
            tree = tree.get(key);
        }
        return tree;
    }
}
