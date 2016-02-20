package se.jhaals;
import junit.framework.TestCase;
import se.jhaals.TokenCreateRequest;
import se.jhaals.TokenCreateRequestBuilder;
import se.jhaals.TokenResponse;
import se.jhaals.Vault;
import se.jhaals.VaultException;
import se.jhaals.VaultResponse;
import se.jhaals.VaultStatus;

import org.junit.Before;

import java.util.HashMap;

public class VaultTest extends TestCase {

    private String token ="58814236-a03e-ed0d-3a5a-d49ecc941c8e";
    private Vault vault;

    @Before
    public void setUp() {
        this.vault = new Vault("http://127.0.0.1:8200", token);
    }

    public void testWrite() throws Exception {

        HashMap<String, String> data = new HashMap<>();
        data.put("value", "hello");
        vault.write("secret/hello", data);
    }

    public void testRead() throws Exception {
        VaultResponse result = vault.read("secret/hello");
        assertEquals(result.getData().get("value"), "hello");
    }

    public void testDelete() throws Exception {
        vault.delete("secret/hello");
        try {
            vault.read("secret/hello");
            fail("Expected VaultException");
        } catch (VaultException e) {
            assertEquals(e.getStatusCode(), 404);

        }
    }

    public void testReadWithInvalidToken() throws Exception {
        Vault vault = new Vault("http://127.0.0.1:8200", "invalid");
        try {
            vault.read("secret/hello");
            fail("Expected VaultException");
        } catch (VaultException e) {
            assertEquals(e.getStatusCode(), 403);
        }
    }

    public void testGetStatus() throws Exception {
        VaultStatus vaultStatus = vault.getStatus();
        assertEquals(vaultStatus.getKeyShares(), 5);
        assertEquals(vaultStatus.getKeyThreshold(), 3);
        assertEquals(vaultStatus.getProgress(), 0);
        assertEquals(vaultStatus.isSealed(), false);
    }

    public void testLookupToken() throws Exception {
       assertEquals(vault.lookupToken(this.token).getData().getDisplayName(), "root");
    }

    public void testCreateToken() throws Exception {
        TokenCreateRequest tokenCreateRequest = new TokenCreateRequestBuilder()
                .setDisplayName("foobar")
                .setNumUses(5)
                .setNoParent(true)
                .createTokenRequest();
        TokenResponse createResult = vault.createToken(tokenCreateRequest);
        TokenResponse lookupResult = vault.lookupToken(createResult.getAuth().getClientToken());
        assertEquals(lookupResult.getData().getDisplayName(), "token-foobar");
        assertEquals(lookupResult.getData().getNumUses(), 5);
        assertEquals(lookupResult.getData().getPolicies().get(0), "root");
    }
}