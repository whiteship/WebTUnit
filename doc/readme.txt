0. add repository setting to pom.xml or settings.xml

		<repository>
			<id>opensprout nexus</id>
			<name>OpenSprout Nexus public</name>
			<url>http://www.opensprout.org/nexus/content/groups/public</url>
		</repository>

		WebTUnit is provided by OpenSprout Nexus.

1. add dependency to pom.xml

		<!-- WebTUnit -->
		<dependency>
			<groupId>org.opensprout</groupId>
			<artifactId>webtunit</artifactId>
			<version>1.0-M1</version>
		</dependency>

2. add cargo-maven-plugin and maven-surefire-plugin to pom.xml

	2-1. cargo-maven-plugin configuraion

		<build>
		...
			<plugin>
				<groupId>org.codehaus.cargo</groupId>
				<artifactId>cargo-maven2-plugin</artifactId>
				<version>1.0.1-SNAPSHOT</version>
				<configuration>
					<wait>false</wait>
					<container>
						<containerId>tomcat6x</containerId>
						<type>installed</type>
						<home>tomcat/apache-tomcat-6.0.18</home>
					</container>
					<deployer>
						<type>installed</type>
					</deployer>
					<configuration>
						<properties>
							<cargo.servlet.port>8080</cargo.servlet.port>
						</properties>
					</configuration>
				</configuration>
				<executions>
					<execution>
						<id>start-container</id>
						<phase>pre-integration-test</phase>
						<goals>
							<goal>start</goal>
						</goals>
					</execution>
					<execution>
						<id>stop-container</id>
						<phase>post-integration-test</phase>
						<goals>
							<goal>stop</goal>
						</goals>
					</execution>
				</executions>
			</plugin>

		First, you must set up your tomcat home to <home> element above or,
		just download and unzip tomcat archive to project/tomcat folder.

		If you set <wait> to true, then when you run a test server by 'mvn cargo:start',
		the console will be wait untile you stop the process by CTRL + C.
		This is useful when you want to test on Eclipse by CTRL + ALT + X -> T or CTRL + F11.
		But, you shold be careful when you use phase that through pre-integration-test phase.
		That process will stop and wait in pre-integration-test until you stop the test server.

		By default setting, you can use cargo with phases thar through pre-integration-test phase.
		For example, when you run 'mvn verify', you will pass pre-integration-test phase,
		but you will not be stopped by process, because it dooesn't wait.
		This will help you when you set up this project in continuous integration environment.

	2-2. maven-surefire-plugin configuration

			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-surefire-plugin</artifactId>
				<configuration>
					<excludes>
						<exclude>**/*WebTest.java</exclude>
					</excludes>
				</configuration>
				<executions>
					<execution>
						<id>integration-test</id>
						<goals>
							<goal>test</goal>
						</goals>
						<phase>integration-test</phase>
						<configuration>
							<excludes>
								<exclude>none</exclude>
							</excludes>
							<includes>
								<include>**/*WebTest.java</include>
							</includes>
						</configuration>
					</execution>
				</executions>
			</plugin>
		...
		</build>

		If you want to use another naming pattern, or package pattern,
		change '**/*WebTest.java' value to what you want.

3. make some web test with webdriver.

	@RunWith(WebTestRunner.class)
	@WarConfiguration("opensprout")
	public class MemberWebTest {

		@WebTest
		public void memberPages(){
			WebDriver driver = new HtmlUnitDriver();
			driver.get("http://localhost:8090/springsprout/member/list.do");
			MemberListPage listPage = PageFactory.initElements(driver, MemberListPage.class);
			assertEquals(2, listPage.getTableRows());
		}
	}

	If you want to feel comfortable when you write some web test codes,
	I recommend Page Object Pattern(http://code.google.com/p/webdriver/wiki/PageObjects).
	
	You can also use @DataConfiguration when you want to input and delete some test code.
	Check some usecases.
	- http://whiteship.me/2237
	- http://whiteship.me/2238
	- http://whiteship.me/2239

4. run test.

	Open a console, type 'mvn verify'.
	Or, modify, cargo-maven-plugin's <wait> to true, and run it with JUnit in Eclipse.
	
5. give me a feedback

	http://github.com/whiteship/WebTUnit/issues
	
	