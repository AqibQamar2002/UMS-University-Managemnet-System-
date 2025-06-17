// Fetch all courses
function fetchCourses() {
    fetch('/university-management-1.0-SNAPSHOT/courses')
        .then(res => res.json())
        .then(data => {
            const tbody = document.querySelector('#coursesTable tbody');
            tbody.innerHTML = '';
            if (data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" class="empty-state">No courses found.</td></tr>';
            } else {
                data.forEach(c => {
                    tbody.innerHTML += `<tr>
                        <td>${c.courseId}</td>
                        <td>${c.courseName}</td>
                        <td>${c.courseCode}</td>
                        <td>${c.credits}</td>
                        <td>
                            <button class="action-btn" onclick="editCourse(${c.courseId}, '${c.courseName}', '${c.courseCode}', ${c.credits})">Edit</button>
                            <button class="action-btn delete" onclick="deleteCourse(${c.courseId})">Delete</button>
                        </td>
                    </tr>`;
                });
            }
        });
}

// Add new course
document.getElementById('courseForm').onsubmit = function(e) {
    e.preventDefault();
    const form = e.target;
    fetch('/university-management-1.0-SNAPSHOT/courses', {
        method: 'POST',
        body: new URLSearchParams(new FormData(form))
    }).then(() => {
        form.reset();
        fetchCourses();
    });
};
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
        if (!data.authenticated || data.role !== 'ADMIN') {
            window.location.href = 'login.html';
        } else {
            document.getElementById('userName').textContent = data.name;
        }
    })
    .catch(() => {
        window.location.href = 'login.html';
    });
}

// Delete course
function deleteCourse(id) {
    if (confirm('Are you sure you want to delete this course?')) {
        fetch('/university-management-1.0-SNAPSHOT/courses?id=' + id, { method: 'DELETE' })
            .then(() => fetchCourses());
    }
}

// Edit course modal logic
const courseModal = document.getElementById('editCourseModal');
const closeCourseModal = document.getElementById('closeEditCourseModal');

closeCourseModal.onclick = function() { 
    courseModal.style.display = 'none'; 
};

window.onclick = function(event) { 
    if (event.target == courseModal) courseModal.style.display = 'none'; 
};

function editCourse(id, name, code, credits) {
    document.getElementById('editCourseId').value = id;
    document.getElementById('editCourseName').value = name;
    document.getElementById('editCourseCode').value = code;
    document.getElementById('editCourseCredits').value = credits;
    courseModal.style.display = 'block';
}

// Update course
document.getElementById('editCourseForm').onsubmit = function(e) {
    e.preventDefault();
    const form = e.target;
    const params = new URLSearchParams(new FormData(form));
    params.append('_method', 'PUT');
    fetch('/university-management-1.0-SNAPSHOT/courses', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        courseModal.style.display = 'none';
        fetchCourses();
    })
    .catch(error => {
        console.error('Error updating course:', error);
        alert('Error updating course. Please try again.');
    });
};

// Initial check for authentication
checkAuth();
// Initial load
fetchCourses(); 