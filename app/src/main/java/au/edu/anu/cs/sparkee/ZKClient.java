package au.edu.anu.cs.sparkee;

import au.edu.anu.cs.sparkee.helper.SHA1;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import android.util.Base64;
import android.util.Log;

import java.util.HashMap;

public class ZKClient {

    /*
        Variables for system setup, includes Pairing pairing and Field G, Zr, GT
        We only have one G (not G1 and G2) because we use symmetric billinear setting.
        http://gas.dia.unisa.it/projects/jpbc/docs/pairing.html#.YJdzmSYxVH4
    */
    public Pairing pairing;
    private Field G;
    public Field Zr;

    // Variables for generator function g and h.
    private Element g, h;

    /*
        Variables for user credentials.
        s = user secret
        q = user unique identifier (changed for each successfull credit claim)
        b = user balance

        s_ = s' = user random secret, used for first registration (1 time use only)
     */
    public Element s, q, b;
    public Element commitment_s, commitment_q, commitment_b;
    private Element s_; // used only for first time registration
    private Element mask_q; // used for credit claiming protocol (when updating q, we need mask_q)
    private Element commitment_random_s, Zs; // nzkpCm[s] data

    // Credential Signature
    private String signQ;

    // User Submission and credit claiming detail. Consists of j, t, T
    String j;
    String t;
    Element T;

    public ZKClient() {
        // setup properties to pick the base graph function
        pairing = PairingFactory.getPairing("assets/a.properties");
        // initialize the Groups based on the graph function
        G = pairing.getG1();
        Zr = pairing.getZr();

        // [DEBUG message] indicating initialization is complete
        System.out.println("Client constructor initialized.");
    }

    /**
     This function is used to set cryptographic information received from the server.
     */
    public void set_crypt_info(HashMap<String, String> crypt_info) {
        g = stringToGElement(crypt_info.get("g"));
        h = stringToGElement(crypt_info.get("h"));
    }

    /**
     This function will create a.properties secret s' (because s is going to be created in this registration process) and a.properties unique identifier q.
     It will also generate a.properties "random" random_s' and random_q for commitment usage and when computing zs' and zq
     Then, it will compute the commitment for s' and q.
     It will also compute the hash h for hash_function(s', q, random_s1, random_q)
     Then, it will compute Zs' = random_s' + h.s' and Zq = random_q + h.q
     Lastly, it will return Zs and Zq.
     */
    public HashMap<String, String> create_registration_data() {
        // generate s', random s', q, random q, and their masks
        s_ = Zr.newRandomElement().getImmutable(); // initialized above (class var)
        Element random_s_ = Zr.newRandomElement().getImmutable();

        q = Zr.newRandomElement().getImmutable(); // initialized above (class var)
        Element random_q = Zr.newRandomElement().getImmutable();

        Element mask_s_ = Zr.newRandomElement().getImmutable();
        Element mask_random_s_ = Zr.newRandomElement().getImmutable();
        mask_q = Zr.newRandomElement().getImmutable();
        Element mask_random_q = Zr.newRandomElement().getImmutable();

        // create commitments
        Element commitment_s_ = commitment(s_, mask_s_);
        commitment_q = commitment(q, mask_q); // initialized above (class var)
        Element commitment_random_s_ = commitment(random_s_, mask_random_s_);
        Element commitment_random_q = commitment(random_q, mask_random_q);


        // generate the hash of commitments, used in non-interactive ZK as generating challenge.
        String hash_input = elementToString(commitment_s_) + elementToString(commitment_q) +
                elementToString(commitment_random_s_) + elementToString(commitment_random_q);
        Element hash = hash_function(hash_input);

        // generate Zs and Zq where Zs = random_s' + h.s' and Zq = random_q + h.q
        Element Zs = random_s_.add(s_.mulZn(hash));
        Element Zq = random_q.add(q.mulZn(hash));
        Element Z_mask_s = mask_random_s_.add(mask_s_.mulZn(hash));
        Element Z_mask_q = mask_random_q.add(mask_q.mulZn(hash));


        HashMap<String, String> regis_param = new HashMap<>();
        regis_param.put("Zs", elementToString(Zs));
        regis_param.put("Zq", elementToString(Zq));
        regis_param.put("Z_mask_s", elementToString(Z_mask_s));
        regis_param.put("Z_mask_q", elementToString(Z_mask_q));
        regis_param.put("commitment_s_", elementToString(commitment_s_));
        regis_param.put("commitment_q", elementToString(commitment_q));
        regis_param.put("commitment_random_s_", elementToString(commitment_random_s_));
        regis_param.put("commitment_random_q", elementToString(commitment_random_q));
        return regis_param;
    }

    public void configure_credentials(HashMap<String, String> regis_result) {
        boolean regis_success = Boolean.parseBoolean(regis_result.get("regis_success"));
        if (!regis_success) {
            // [DEBUG message]
            Log.d("[GG] ZK_CLIENT", "Client registration process failed");
            return;
        }

        b = stringToZrElement(regis_result.get("b"));
        s = stringToZrElement(regis_result.get("s__")).add(s_);
        commitment_b = stringToGElement(regis_result.get("commitment_b"));
        commitment_s = stringToGElement(regis_result.get("commitment_s"));
        signQ = regis_result.get("signQ");
        generate_nzkpCm_s_data();

    }

    /**
     Function used by client to generate crowdsensed data.
     j = parking location at spot j
     t = time t
     a.properties = availability of parking at location j at time t (True = available)

     compute:
     1. H(j|t)
     2. T = Cm(s, H(j|t))
     3. nzkpCm[s]
     send as part of the hashmap:
     1. R = (j, t, T, a.properties)
     2. nzkpCm[s]
     */
    public HashMap<String, String> create_submission_data(String j, String t, boolean a) {
        // Compute hash H(j|t)
        this.j = j;
        this.t = t;
        String hash_input = j + t; // concat j and t
        Element hash_jt = hash_function(hash_input);

        // Compute Ticket T = Cm(s, H(j|t))
        T =commitment(s, hash_jt); // initiated in constructor

        // compute data needed for nzkpCm[s]
        HashMap<String, String> submission_data = get_nzkpCm_s_data();

        submission_data.put("j", j);
        submission_data.put("t", t);
        submission_data.put("a", Boolean.toString(a));
        submission_data.put("T", elementToString(T));

        return submission_data;
    }

    /**
     Function for creating credit claim data upon data submission.
     */
    public HashMap<String, String> create_credit_claim_data() {
        HashMap<String, String> credit_claim_data = get_nzkpCm_s_data();

        credit_claim_data.put("j", j);
        credit_claim_data.put("t", t);
        credit_claim_data.put("T", elementToString(T));
        credit_claim_data.put("commitment_s", elementToString(commitment_s));
        credit_claim_data.put("commitment_q", elementToString(commitment_q));
        credit_claim_data.put("commitment_b", elementToString(commitment_b));
        credit_claim_data.put("signQ", signQ);

        return credit_claim_data;
    }

    public HashMap<String, String> get_q_and_mask_q() {
        HashMap<String, String> client_q_data = new HashMap<>();
        client_q_data.put("q", elementToString(q));
        client_q_data.put("mask_q", elementToString(mask_q));
        client_q_data.put("commitment_q", elementToString(commitment_q));

        // [DEBUG] message
        Log.d("[GG] ZK_CLIENT","Client created credit claim data successfully");

        return client_q_data;
    }

    public HashMap<String, String> compute_new_q() {

        HashMap<String, String> q_data = new HashMap<>();

        // update current q value to new_q
        Element old_q = q; // not needed when implemented in WS
        q = Zr.newRandomElement().getImmutable();
        mask_q = Zr.newRandomElement().getImmutable();
        commitment_q = commitment(q, mask_q);

        // return Cm(q') to server
        q_data.put("commitment_random_new_q", elementToString(commitment_q));
        q_data.put("commitment_s", elementToString(commitment_s));
        q_data.put("commitment_b", elementToString(commitment_b));
        q_data.put("q", elementToString(old_q));
        q_data.put("T", elementToString(T));

        // [DEBUG] message
        Log.d("[GG] ZK_CLIENT","Client computed new_q successfully.");

        return q_data;
    }

    public void accept_reward(HashMap<String, String> server_reward) {
        // get reward_data
        Element c = stringToZrElement(server_reward.get("c"));
        Element commitment_new_b = stringToGElement(server_reward.get("commitment_new_b"));
        signQ = server_reward.get("signQ");

        // update balance data
        b = b.add(c); // new_balance = now_balance + reward_balance
        commitment_b = commitment_new_b;

        // [DEBUG] message
        Log.d("[GG] ZK_CLIENT","Client accepted server reward. Current balance is: " + b);
    }

    private Element commitment(Element x, Element r){
        // compute g ^ x . h ^ r
        return g.powZn(x).mul(h.powZn(r));
    }

    private void generate_nzkpCm_s_data() {
        // create  Cm(s', 0). => the mask is 0 so that server can compute Zr on its own.
        Element random_s = Zr.newRandomElement().getImmutable();
        Element mask_random_s = Zr.newElement(0).getImmutable(); // mask is 0
        commitment_random_s = commitment(random_s, mask_random_s);

        // create hash = H(Cm(s', 0))
        String hash_input = elementToString(commitment_random_s);
        Element hash_Cm_random_s = hash_function(hash_input);

        // compute Zs = s' + H(Cm(s', 0)) . s
        Zs = random_s.add(s.mulZn(hash_Cm_random_s));
    }

    private HashMap<String, String> get_nzkpCm_s_data() {
        HashMap<String, String> nzkpCm_s_data = new HashMap<>();
        nzkpCm_s_data.put("commitment_random_s", elementToString(commitment_random_s));
        nzkpCm_s_data.put("Zs", elementToString(Zs));

        return nzkpCm_s_data;
    }
    /**
     * Utility function take a.properties pbc element and output its string representation.
     * @param e
     * @return Base64 encoded string
     */
    public String elementToString(Element e){
        byte[] temp = e.toBytes();
        String ret = Base64.encodeToString(temp, Base64.DEFAULT);
        ret = ret.replace("\n", "");
        return ret;
    }

    /**
     * Utility function take a.properties string representation and generate the pbc element from group Zr
     * @param s
     * @return
     */
    public Element stringToZrElement(String s){
        Element ret = Zr.newElementFromBytes(Base64.decode(s, Base64.DEFAULT)).getImmutable();
        return ret;
    }

    /**
     * Utility function take a.properties string representation and generate the pbc element from group G
     * @param s
     * @return
     */
    public Element stringToGElement(String s){
        Element ret = G.newElementFromBytes(Base64.decode(s, Base64.DEFAULT)).getImmutable();
        return ret;
    }

    private Element hash_function(String hash_input) {
        String sha = SHA1.hash(hash_input);
        Element result = stringToGElement(sha);
        return result;
    }

    public String getS() {
        return elementToString(s);
    }

    public String getQ() {
        return elementToString(q);
    }

    public String getB() {
        return elementToString(b);
    }

    public String getReadableBalance() {
        return b.toString();
    }

    public String getCommitment_s() {
        return elementToString(commitment_s);
    }

    public String getCommitment_q() {
        return elementToString(commitment_q);
    }

    public String getCommitment_b() {
        return elementToString(commitment_b);
    }

    public String getS_() {
        return elementToString(s_);
    }

    public String getMask_q() {
        return elementToString(mask_q);
    }

    public String getCommitment_random_s() {
        return elementToString(commitment_random_s);
    }

    public String getZs() {
        return elementToString(Zs);
    }

    public String getSignQ() {
        return signQ;
    }


    public void setS(String str) {
        s = stringToZrElement(str);
    }

    public void setQ(String str) {
        q = stringToZrElement(str);
    }

    public void setB(String str) {
        b = stringToZrElement(str);
    }

    public void setCommitment_s(String str) {
        commitment_s = stringToGElement(str);
    }

    public void setCommitment_q(String str) {
        commitment_q = stringToGElement(str);
    }

    public void setCommitment_b(String str) {
        commitment_b = stringToGElement(str);
    }

    public void setS_(String str) {
        s_ = stringToZrElement(str);
    }

    public void setMask_q(String str) {
        mask_q = stringToZrElement(str);
    }

    public void setCommitment_random_s(String str) {
        commitment_random_s = stringToGElement(str);
    }

    public void setZs(String str) {
        Zs = stringToZrElement(str);
    }

    public void setSignQ(String str) {
        signQ = str;
    }
}