import React, { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import Card, { CardHeader, CardBody } from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import { Clock, Calendar, MapPin, User, BookOpen } from 'lucide-react';

const DAYS = ['ALL', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'];

export const StudentTimetablePage = () => {
  const { data } = useOutletContext();
  const [selectedDay, setSelectedDay] = useState('ALL');

  const timetables = data?.timetables || [];
  const todayClasses = data?.todayClasses || [];
  const section = data?.section || 'C';
  const department = data?.department || 'Computer Science & Engineering';

  const filteredTimetables = timetables.filter((item) => {
    if (selectedDay === 'ALL') return true;
    return item.dayOfWeek?.toLowerCase() === selectedDay.toLowerCase();
  });

  // Group by day of week if 'ALL'
  const groupedTimetables = DAYS.slice(1).reduce((acc, day) => {
    acc[day] = timetables.filter((t) => t.dayOfWeek?.toLowerCase() === day.toLowerCase());
    return acc;
  }, {});

  return (
    <div className="space-y-6">
      {/* Timetable Header Card */}
      <Card>
        <CardHeader
          title={`Weekly Academic Master Timetable: ${department} (Section ${section})`}
          subtitle="Official institutional lecture & laboratory schedule"
          action={
            <div className="flex items-center gap-1.5 p-1 rounded-xl bg-[var(--color-muted)]/40 border border-[var(--color-border)] overflow-x-auto no-scrollbar">
              {DAYS.map((day) => (
                <button
                  key={day}
                  onClick={() => setSelectedDay(day)}
                  className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all shrink-0 ${
                    selectedDay === day
                      ? 'bg-[var(--color-card)] text-[var(--color-foreground)] shadow-xs'
                      : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
                  }`}
                >
                  {day}
                </button>
              ))}
            </div>
          }
        />
        <CardBody className="p-0">
          {selectedDay === 'ALL' ? (
            <div className="divide-y divide-[var(--color-border)]">
              {DAYS.slice(1).map((day) => {
                const dayItems = groupedTimetables[day] || [];
                if (dayItems.length === 0) return null;

                return (
                  <div key={day} className="p-5 space-y-3">
                    <div className="flex items-center gap-2">
                      <Calendar className="w-4 h-4 text-[var(--color-primary)]" />
                      <h3 className="text-xs font-bold uppercase tracking-wider text-[var(--color-foreground)]">
                        {day}
                      </h3>
                      <span className="text-[10px] text-[var(--color-muted-foreground)]">
                        ({dayItems.length} periods)
                      </span>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                      {dayItems.map((item, idx) => (
                        <div
                          key={idx}
                          className="p-3.5 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)]/50 space-y-2 hover:border-[var(--color-primary)]/40 transition-colors"
                        >
                          <div className="flex items-center justify-between">
                            <span className="font-mono text-xs font-bold text-[var(--color-primary)] px-1.5 py-0.5 rounded bg-[var(--color-primary)]/10">
                              {item.startTime} - {item.endTime}
                            </span>
                            <span className="font-mono text-[10px] font-bold px-1.5 py-0.5 rounded bg-[var(--color-muted)] text-[var(--color-foreground)]">
                              {item.subjectCode}
                            </span>
                          </div>
                          <div>
                            <p className="text-xs font-bold text-[var(--color-foreground)]">{item.subjectName}</p>
                            <p className="text-[11px] text-[var(--color-muted-foreground)] mt-1 flex items-center gap-1.5">
                              <User className="w-3 h-3 text-[var(--color-primary)]" />
                              <span>{item.facultyName}</span>
                            </p>
                            <p className="text-[11px] text-[var(--color-muted-foreground)] mt-0.5 flex items-center gap-1.5">
                              <MapPin className="w-3 h-3 text-emerald-500" />
                              <span className="font-medium text-[var(--color-foreground)]">{item.classroom}</span>
                            </p>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="p-5">
              {filteredTimetables.length === 0 ? (
                <p className="text-xs text-center text-[var(--color-muted-foreground)] py-8">
                  No classes scheduled for {selectedDay}.
                </p>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  {filteredTimetables.map((item, idx) => (
                    <div
                      key={idx}
                      className="p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)]/50 space-y-2.5"
                    >
                      <div className="flex items-center justify-between">
                        <span className="font-mono text-xs font-bold text-[var(--color-primary)] px-2 py-0.5 rounded bg-[var(--color-primary)]/10">
                          {item.startTime} - {item.endTime}
                        </span>
                        <span className="font-mono text-[10px] font-bold px-1.5 py-0.5 rounded bg-[var(--color-muted)] text-[var(--color-foreground)]">
                          {item.subjectCode}
                        </span>
                      </div>
                      <div>
                        <h4 className="text-xs font-bold text-[var(--color-foreground)]">{item.subjectName}</h4>
                        <p className="text-[11px] text-[var(--color-muted-foreground)] mt-1 flex items-center gap-1.5">
                          <User className="w-3 h-3 text-[var(--color-primary)]" />
                          <span>{item.facultyName}</span>
                        </p>
                        <p className="text-[11px] text-[var(--color-muted-foreground)] mt-0.5 flex items-center gap-1.5">
                          <MapPin className="w-3 h-3 text-emerald-500" />
                          <span className="font-medium text-[var(--color-foreground)]">{item.classroom}</span>
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </CardBody>
      </Card>
    </div>
  );
};

export default StudentTimetablePage;
