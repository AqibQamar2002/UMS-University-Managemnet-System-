// Fetch available courses for students
function fetchAvailableCourses() {
    fetch('/university-management-1.0-SNAPSHOT/courses')
        .then(res => res.json())
        .then(courses => {
            const coursesGrid = document.getElementById('availableCourses');
            if (courses.length === 0) {
                coursesGrid.innerHTML = '<p class="empty-state">No courses available at the moment.</p>';
                return;
            }

            coursesGrid.innerHTML = courses.map(course => `
                <div class="course-card">
                    <h3>${course.courseName}</h3>
                    <div class="course-details">
                        <p><strong>Code:</strong> ${course.courseCode}</p>
                        <p><strong>Credits:</strong> ${course.credits}</p>
                        <p><strong>Instructor:</strong> ${course.instructor || 'To be assigned'}</p>
                        <p><strong>Schedule:</strong> ${course.schedule || 'To be announced'}</p>
                        <p><strong>Description:</strong> ${course.description || 'No description available'}</p>
                    </div>
                    <button class="enroll-btn" onclick="enrollInCourse(${course.courseId})" 
                            ${course.enrollmentStatus === 'ENROLLED' ? 'disabled' : ''}>
                        ${course.enrollmentStatus === 'ENROLLED' ? 'Enrolled' : 
                          course.enrollmentStatus === 'PENDING' ? 'Request Pending' : 'Enroll'}
                    </button>
                </div>
            `).join('');
        })
        .catch(error => {
            console.error('Error fetching courses:', error);
            document.getElementById('availableCourses').innerHTML = 
                '<p class="error-state">Error loading courses. Please try again later.</p>';
        });
}

// Enroll in a course
function enrollInCourse(courseId) {
    
    fetch('/university-management-1.0-SNAPSHOT/enrolled-courses', {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ action: 'enroll', courseId })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert('Enrollment request submitted successfully!');
            fetchAvailableCourses();
        } else {
            alert('Failed to submit enrollment request: ' + data.message);
        }
    })
    .catch(error => {
        console.error('Error enrolling in course:', error);
        alert('Error submitting enrollment request. Please try again later.');
    });
}

// Fetch enrolled courses
function fetchEnrolledCourses() {
    fetch('/university-management-1.0-SNAPSHOT/enrolled-courses', {
        method: 'GET',
        credentials: 'include'
    })
    .then(response => response.json())
    .then(courses => {
        const coursesGrid = document.getElementById('enrolledCourses');
        if (courses.length === 0) {
            coursesGrid.innerHTML = '<p class="empty-state">You are not enrolled in any courses yet.</p>';
            return;
        }

        coursesGrid.innerHTML = courses.map(course => `
            <div class="course-card">
                <h3>${course.courseName}</h3>
                <div class="course-details">
                    <p><strong>Code:</strong> ${course.courseCode}</p>
                    <p><strong>Credits:</strong> ${course.credits}</p>
                    <p><strong>Instructor:</strong> ${course.instructor || 'To be assigned'}</p>
                    <p><strong>Schedule:</strong> ${course.schedule || 'To be announced'}</p>
                    <p><strong>Status:</strong> 
                        <span class="status-badge status-${course.status.toLowerCase()}">
                            ${course.status}
                        </span>
                    </p>
                </div>
                <button class="enroll-btn" onclick="unenrollFromCourse(${course.courseId})">
                    Unenroll
                </button>
            </div>
        `).join('');
    })
    .catch(error => {
        console.error('Error fetching enrolled courses:', error);
        document.getElementById('enrolledCourses').innerHTML = 
            '<p class="error-state">Error loading enrolled courses. Please try again later.</p>';
    });
}

// Unenroll from a course
function unenrollFromCourse(courseId) {
    if (confirm('Are you sure you want to unenroll from this course?')) {
        fetch('/university-management-1.0-SNAPSHOT/enrolled-courses', {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ action: 'unenroll', courseId })
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                alert('Successfully unenrolled from the course!');
                fetchEnrolledCourses();
                fetchAvailableCourses();
            } else {
                alert('Failed to unenroll: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error unenrolling from course:', error);
            alert('Error unenrolling from course. Please try again later.');
        });
    }
}

// Fetch pending requests
function fetchPendingRequests() {
    fetch('/university-management-1.0-SNAPSHOT/enrolled-courses?action=pending', {
        method: 'GET',
        credentials: 'include'
       
    })
    .then(response => response.json())
    .then(requests => {
        const pendingGrid = document.getElementById('pendingCourses');
        if (requests.length === 0) {
            pendingGrid.innerHTML = '<p class="empty-state">No pending enrollment requests.</p>';
            return;
        }
        pendingGrid.innerHTML = requests.map(req => `
            <div class="course-card">
                <h3>${req.courseName}</h3>
                <div class="course-details">
                    <p><strong>Code:</strong> ${req.courseCode}</p>
                    <p><strong>Credits:</strong> ${req.credits}</p>
                    <p><strong>Instructor:</strong> ${req.instructor || 'To be assigned'}</p>
                    <p><strong>Schedule:</strong> ${req.schedule || 'To be announced'}</p>
                    <p><strong>Status:</strong> <span class="status-badge status-pending">${req.status}</span></p>
                    <p><strong>Requested On:</strong> ${new Date(req.requestDate).toLocaleString()}</p>
                </div>
            </div>
        `).join('');
    })
    .catch(error => {
        console.error('Error fetching pending requests:', error);
        document.getElementById('pendingCourses').innerHTML =
            '<p class="error-state">Error loading pending requests. Please try again later.</p>';
    });
}

// Show selected tab
function showTab(tabName) {
    document.querySelectorAll('.tab').forEach(tab => tab.classList.remove('active'));
    document.querySelector(`.tab[onclick="showTab('${tabName}')"]`).classList.add('active');
    
    document.getElementById('availableCourses').style.display = tabName === 'available' ? 'grid' : 'none';
    document.getElementById('enrolledCourses').style.display = tabName === 'enrolled' ? 'grid' : 'none';
    document.getElementById('pendingCourses').style.display = tabName === 'pending' ? 'grid' : 'none';
    if (tabName === 'pending') fetchPendingRequests();
}
// Logout function
function logout() {
    fetch('/university-management-1.0-SNAPSHOT/logout', {
        method: 'POST',
        credentials: 'include'
    })
    .then(() => {
        window.location.href = 'login.html';
    });
}
function checkAuth() {
    fetch('/university-management-1.0-SNAPSHOT/check-auth', {
        method: 'GET',
        credentials: 'include'
    })
    .then(response => response.json())
    .then(data => {
        if (!data.authenticated) {
            window.location.href = 'login.html';
        } else {
            document.getElementById('userName').textContent = data.name;
        }
    })
    .catch(() => {
        window.location.href = 'login.html';
    });
}
// Initial check for authentication
checkAuth();
// Initial load
fetchAvailableCourses();
fetchEnrolledCourses(); 