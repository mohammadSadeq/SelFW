package com.generic.tests.account;

import static org.testng.Assert.assertNotEquals;

import java.text.MessageFormat;

import org.apache.commons.lang3.RandomUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;
import java.util.LinkedHashMap;

import com.generic.page.Registration;
import com.generic.page.AddressBook;
import com.generic.page.CheckOut;
import com.generic.page.SignIn;
import com.generic.selector.AddressBookSelectors;
import com.generic.setup.Common;
import com.generic.setup.LoggingMsg;
import com.generic.setup.SelTestCase;
import com.generic.setup.SheetVariables;
import com.generic.util.TestUtilities;
import com.generic.util.ReportUtil;
import com.generic.util.SASLogger;
import com.generic.util.SelectorUtil;

public class AddressBookValidation extends SelTestCase {
	private static LinkedHashMap<String, Object> addresses = null;
	private static LinkedHashMap<String, Object> users = null;
	boolean defaultAddress = true;
	private String addressbook;
	private int numberofaddresses;
	// used sheet in test
	public static final String testDataSheet = SheetVariables.AddressBookSheet;

	private String caseId;
	private String runTest;
	private String url;
	private String desc;
	private String newaddress;
	private String email;

	private static XmlTest testObject;

	private static ThreadLocal<SASLogger> Testlogs = new ThreadLocal<SASLogger>();

	@BeforeTest
	public static void initialSetUp(XmlTest test) throws Exception {
		addresses = Common.readAddresses();
		users = Common.readUsers();
		testObject = test;
	}

	@DataProvider(name = "AddressBook", parallel = true)
	public static Object[][] loadTestData() throws Exception {
		// concurrency maintenance on sheet reading
		getBrowserWait(testObject.getParameter("browserName"));
		Object[][] data = TestUtilities.getData(testDataSheet);
		return data;
	}

	@SuppressWarnings("unchecked") // avoid warning from linked hashmap
	@Test(dataProvider = "AddressBook")
	public void BookAddressTest(String caseId, String runTest, String url, String desc, String email, String newaddress)
			throws Exception {
		// Important to add this for logging/reporting
		Testlogs.set(new SASLogger("Address_Book " + getBrowserName()));
		setTestCaseReportName("Address Book Case");
		logCaseDetailds(MessageFormat.format(LoggingMsg.ADDRESSPOOKDESC, testDataSheet + "." + caseId,
				this.getClass().getCanonicalName(), desc));

		this.email = email.replace("tester", "tester_" + getBrowserName().replace(" ", "_"));

		try {
			LinkedHashMap<String, Object> userdetails = (LinkedHashMap<String, Object>) users.get(email);
			Testlogs.get().debug(this.email);
			Testlogs.get().debug((String) userdetails.get(Registration.keys.password));
			
			if (desc.contains("edit")) {
				SignIn.logIn(this.email, (String) userdetails.get(Registration.keys.password));
				getDriver().get(url);
				addressbook = AddressBook.getFirstAddressDetails();
				AddressBook.clickEditAddress();
				AddressBook.updateAddress();
				AddressBook.clickAddressBackBtn();
				assertNotEquals(addressbook, AddressBook.getFirstAddressDetails());
			}
			if (desc.contains("form validation")) {
				SignIn.logIn(this.email, (String) userdetails.get(Registration.keys.password));
				getDriver().get(url);
				addressbook = AddressBook.getFirstAddressDetails();
				AddressBook.clickEditAddress();
				AddressBook.clearAddress();
				AddressBook.verifyAddressFormError();
			}
			if (desc.contains("new") || desc.contains("default") || desc.contains("delete")) {
				SignIn.logIn(this.email, (String) userdetails.get(Registration.keys.password));
				getDriver().get(url);
				addressbook = AddressBook.getFirstAddressDetails();
				Thread.sleep(1000);
				AddressBook.clickAddNewAddress();
				getDriver().get(AddressBookSelectors.addaddressurl);
				LinkedHashMap<String, Object> addressDetails = (LinkedHashMap<String, Object>) addresses
						.get(newaddress);
				logs.debug("test");
				AddressBook.fillAndClickSave((String) addressDetails.get(CheckOut.shippingAddress.keys.countery),
						(String) addressDetails.get(CheckOut.shippingAddress.keys.title),
						"NEW_"+RandomUtils.nextInt(1000,9000),
						(String) addressDetails.get(CheckOut.shippingAddress.keys.lastName),
						(String) addressDetails.get(CheckOut.shippingAddress.keys.adddressLine),
						(String) addressDetails.get(CheckOut.shippingAddress.keys.city),
						(String) addressDetails.get(CheckOut.shippingAddress.keys.postal),
						(String) addressDetails.get(CheckOut.shippingAddress.keys.phone), defaultAddress);
				AddressBook.clickAddressBackBtn();
				sassert().assertNotEquals(addressbook, AddressBook.getFirstAddressDetails());
			
			if (desc.contains("default")) {
				getDriver().get(url);
				addressbook = AddressBook.getFirstAddressDetails();
				AddressBook.clickSetAsDefault();
				AddressBook.getAlertInfo();
				sassert().assertNotEquals(addressbook, AddressBook.getFirstAddressDetails());
			}
			if (desc.contains("delete")) {
				getDriver().get(url);
				AddressBook.getAddressBookList();
				logs.debug(MessageFormat.format(LoggingMsg.ACTUAL_TEXT, SelectorUtil.numberOfFoundElements));
				numberofaddresses = SelectorUtil.numberOfFoundElements;
				AddressBook.clickRemoveAddress();
				AddressBook.clickDeleteBtn();
				Thread.sleep(7000);
				AddressBook.getAddressBookList();
				logs.debug(MessageFormat.format(LoggingMsg.ACTUAL_TEXT, SelectorUtil.numberOfFoundElements));
				sassert().assertNotEquals(numberofaddresses, SelectorUtil.numberOfFoundElements);
			}
			}
			sassert().assertAll();
			Common.testPass();
		} catch (Throwable t) {
			setTestCaseDescription(getTestCaseDescription());
			Testlogs.get().debug(MessageFormat.format(LoggingMsg.DEBUGGING_TEXT, t.getMessage()));
			t.printStackTrace();
			String temp = getTestCaseReportName();
			Common.testFail(t, temp);
			ReportUtil.takeScreenShot(getDriver());
			Assert.assertTrue(false, t.getMessage());
		} // catch
	}// test
}// class