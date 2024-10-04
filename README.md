# CS 122B Project 1 For CDC Disney
### Authors: 
#### - Jiayun Wang, JiaLiang Huang

### Contributions for each member:
#### - Jiayun Wang    #16742752 
#### - Jialiang Huang    #41101462


#### Project 1
#### Jiayun Wang - Servlets + Json + Github + MySQL + Debug + Demo
#### Jialiang Huang - HTML + CSS + AWS + Jump Function + Project setup / management

#### Project 2
#### Jiayun Wang - Servlet + Js + HTML + Github + MySQL + Debug + Demo
#### Jialiang Huang - HTML + Project setup +  + CSS + AWS + Session/Session Storage

#### Project 3
#### Jiayun Wang - reCAPTCHA + Prepared Statement + HTTPS + Demo
#### Jialiang Huang - HTTPS + XML+ Encryption + Dashboard
All insertion Optimizations:

1. Batch Insertion: Rather than inserting records one at a time, batch insertion enables the database to process multiple records simultaneously. This significantly reduces overhead in establishing database transactions, leading to a substantial increase in operational speed.

2. Caching Not Found Entries: 'NotFoundStars' and 'NotFoundMovies' serve as caches for stars and movies that are searched for but not present in the database. This system prevents unnecessary repeat queries to the database, saving time on searching for non-existent entries.

3. Index Creation: Created indexes on the 'star' table (name column) and 'movies' table (title column). This optimization considerably speeds up data retrieval by enabling faster access to data associated with specific values, enhancing the efficiency of fetching records of stars and movies.

4. Handling Bad Data: Tracking erroneous or irrelevant data helps in avoiding repetitive mistakes and saves time on processing non-useful data. This involves maintaining a list of records that have caused errors or have been flagged as incorrect in the past.

5. Efficient Data Structures: Utilized efficient data structures for storing and manipulating data. Arrays provide quick access to elements by index, while hash maps offer rapid retrieval of values associated with specific keys.
   
#### Extra Credit
1. Use Godaddy to register and purchase a domain link to an external website to deploy correctly to https://movieyee.com/, which may take anywhere from a few hours to 48 hours due to DNS propagation.

2. Correctly deployed DNS, searchable at https://dnschecker.org/

3. Elastic IP. correctly set up AWS inbound and outbound rules

#### Project 4
#### Jiayun Wang - Connection Pooling + Master-Slave Replication + Load balancer
#### Jialiang Huang - Full-text Search + Autocomplete + fuzzy search
#### Connection Pooling:

Explain how Connection Pooling is utilized in the Fabflix code:

In Fabflix's code, the DataSource object is initialized within the servlet's init method by locating the appropriate resource from the context using JNDI. These resources are typically java:comp/env/jdbc/read or java:comp/env/jdbc/moviedb. When a request is handled in the doPost() or doGet() method, a connection is acquired from the connection pool via dataSource.getConnection(). This call manages the pool of available connections, ensuring that a connection is efficiently retrieved from the pool. The retrieved connection is then used to perform database operations. Once the query is executed and necessary operations are completed, the connection is automatically closed and returned to the pool when it goes out of scope.

Explain how Connection Pooling works with two backend SQL:

The context.xml file defines two data sources: one for the read/write master database (jdbc/write) and another for the read-only database (jdbc/read). Connection pooling settings are applied to each data source, creating distinct connection pools for each. When a servlet handles a request, it retrieves a connection from the relevant data source based on the operation required. Write operations obtain connections from the jdbc/write data source, while read operations acquire connections from the jdbc/read data source. Each connection pool manages the allocation and reuse of connections within its pool. By using separate connection pools for each data source, the system ensures that requests are efficiently directed to the appropriate database, enhancing both availability and scalability.

#### Master/Slave:
Servlets that use the read-only datasource:

src/IndexServlet.java
src/DashboardloginServlet.java
src/LoginServlet.java
src/MovieListServlet.java
src/SingleMovieServlet.java
src/SingleStarServlet.java

Servlets that use the read/write datasource:

src/DashboardServlet.java
src/Payment.java

How read/write requests are routed to Master/Slave SQL:
In the context.xml file, there are two defined data sources: one for the read/write master database (jdbc/moviedb) and another for the read-only database (jdbc/read).

#### Project 5
#### Jiayun Wang - Docker + JMeter + README + Debug
#### Jialiang Huang - Kubernetes Setup + AWS + Demo + Fabflix on Kubernetes
#### JMeter TS:
First Image (Test 1)
Number of Samples: 1106
Latest Sample Time: 167 ms
Average Response Time: 290 ms
Median Response Time: 281 ms
Throughput: 2107.066 requests per minute
![1.png](1.png)
Second Image (Test 2)
Number of Samples: 907
Latest Sample Time: 116 ms
Average Response Time: 304 ms
Median Response Time: 307 ms
Throughput: 1933.559 requests per minute
![2.png](2.png)

#### Summary
Test 1 has a higher throughput of 2107.066 requests per minute compared to Test 2 which has a throughput of 1933.559 requests per minute.
The average response time in Test 1 is slightly lower (290 ms) compared to Test 2 (304 ms).
The median response time in Test 1 (281 ms) is also lower than in Test 2 (307 ms).
This indicates that the first configuration (shown in the first image) handles a higher number of requests per minute with slightly better response times compared to the second configuration (shown in the second image).


# Demo Video

### - Project 1 Demo Video here：https://drive.google.com/file/d/1K5AAdRiwdrTMpG3zFNxRlDfILC-0Jx-Q/view?usp=sharing

### - Project 2 Demo Video here: https://drive.google.com/drive/folders/1q9WkuCPKhS-dBRyc8rxXMJ19d77zmLpy?usp=drive_link

### - Project 3 Demo Video here：https://drive.google.com/drive/folders/17BQ_FDlTE-IhTr14YvqtzzFW8-plt0kv?usp=sharing

### - Project 4 Video Demo Link: https://youtu.be/zEL8w_rdQPo?si=OgYUwC6h4Hefk31R

### - Project 5 Video Demo Link: [https://youtu.be/zEL8w_rdQPo?si=OgYUwC6h4Hefk31R](https://www.youtube.com/watch?v=UDOTG-eKS8Y)



# Instructions Login to the AWS

Instance ID: i-00f891bc393235c23 (Will's Service)

1. Obtain SSH access.
2. Ensure the key file is secured. After downloading the key file named KeyForWill.pem,
3. Set proper permissions to ensure the key file is not overly permissive.
```
chmod 400 "KeyForWill.pem"
```
4. Connect using the DNS provided for the instance:
```
ec2-34-216-228-170.us-west-2.compute.amazonaws.com
```

Example Command
```
ssh -i "KeyForWill.pem" ubuntu@ec2-34-216-228-170.us-west-2.compute.amazonaws.com
```


# Mysql database 
### `moviedbe`

```
local> mysql -u mytestuser -p 
(Password: My6$Password)
mysql> SHOW DATABASES;
mysql> USE moviedbe;
```

# Deploy Locally on Development Machine

1. IntelliJ Configuration
2. Import Project from External Model > Choose Maven

